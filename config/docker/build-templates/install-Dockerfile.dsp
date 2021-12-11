#
# Install webMethods components using installer and image specified by SW argument
#

FROM centos:%value args/INSTALL_VERSION% AS INSTALLER

LABEL BUILD=%value id%

RUN yum install -y wget

RUN mkdir /software

RUN useradd -c 'sagadmin user' -m -d /home/sag -s /bin/sh sagadmin 

RUN mkdir /opt/softwareag
RUN chown -R sagadmin /opt/softwareag
RUN chown -R sagadmin:sagadmin /software

%ifvar HOST -notempty%
	#RUN ADD fake fakehostname.c /fakehostname.c
	#RUN gcc -o fakehostname.o -c -fPIC -Wall /fakehostname.c
	#RUN gcc -o libfakehostname.so -shared -W1,export-dynamic fakehostname.o -ldl
%endif%

%ifvar buildCommands%
# files to be added prior to install
	%loop buildCommands%
		%ifvar buildTarget equals('install')%
			%ifvar commandType equals('file')%
# %value description%%nl%
				%ifvar /user%
					ADD --chown=%value /user% %value source% %value target% 
				%else%
					ADD %value source% %value target%
				%endif%
				%ifvar chmod -notempty%
					RUN chmod u+x %value chmod%
				%endif%
			%endif%
		%endif%
	%endloop%
%endif%

# script for SAG installer & update (if required)

%ifvar download -notempty%
ADD --chown=sagadmin ./resources/downloader-script.txt /software
%endif%
ADD --chown=sagadmin ./resources/installer-script.txt /software 
ADD --chown=sagadmin ./resources/update-all-fixes.txt /software

%ifvar buildCommands%
# commands to be executed before install
	%loop buildCommands%
		%ifvar buildTarget equals('pre-install')%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
		%endif%
	%endloop%
%endif%

%ifvar HOST -notempty%
	# SAG installer
	ENV JAVA_HOME /opt/softwareag/jvm/jvm
	ENV install.hostname=%value HOST%%nl%
	RUN echo $(head -1 /etc/hosts | cut -f1) %value HOST% >> /etc/hosts;\
		echo "id `hostname`"; cd /software; su sagadmin -c './install.sh -s host.docker.internal:9090 -i /software/installer-script.txt %ifvar download -notempty%-d /software/downloader-script.txt%else% -w %value installImage% %endif% -u false'

	%ifvar update equals('true')%
	# SAG Updater
		RUN echo $(head -1 /etc/hosts | cut -f1) %value HOST% >> /etc/hosts;\
		echo "id `hostname`"; cd /software; su sagadmin -c './install.sh -s host.docker.internal:9090 -u true'
	%end%
%else%
	USER sagadmin
	env JAVA_HOME /opt/softwareag/jvm/jvm
# SAG installer
	RUN cd /software; ./install.sh -s host.docker.internal:9090 -i /software/installer-script.txt %ifvar download -notempty%-d /software/downloader-script.txt%else% -w %value installImage%%endif% -u false

	%ifvar update equals('true')%
	# SAG updater

		RUN cd /software; ./install.sh -s host.docker.internal:9090 -u true
	%else%
	# clean install files
		RUN rm -Rf /opt/softwareag/install
	%endif%
%endif%

%ifvar buildCommands%
# commands to be executed after installation but before build
	%loop buildCommands%
		%ifvar buildTarget equals('post-install')%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
		%endif%
	%endloop%
%endif%


###########################################################
# Second stage, build and config

FROM %value fromImage% AS BUILDER

LABEL MAINTAINER="%value author%" \
	DESCRIPTION="%value description%" \
	COMMENT="%value buildComments%" \
	CUSTOM="false" \
	SAG="true" \
	TYPE="%value type%" \
	BUILD="%value id%" %ifvar args/primaryPort -notempty%\%nl%
	PRIMARY_PORT="%value args/primaryPort%" %endifvar%

%ifvar fromImage equals('alpine:latest')%

	#alpine specific setup

	RUN addgroup -S sagadmin && adduser -S sagadmin -G sagadmin -s /bin/sh -h /home/sag

	# required in order to run java!!
	
	RUN apk --update add --no-cache ca-certificates curl openssl binutils xz \
	    && GLIBC_VER="2.28-r0" \
	    && ALPINE_GLIBC_REPO="https://github.com/sgerrand/alpine-pkg-glibc/releases/download" \
	    && GCC_LIBS_URL="https://archive.archlinux.org/packages/g/gcc-libs/gcc-libs-8.2.1%2B20180831-1-x86_64.pkg.tar.xz" \
	    && GCC_LIBS_SHA256=e4b39fb1f5957c5aab5c2ce0c46e03d30426f3b94b9992b009d417ff2d56af4d \
	    && ZLIB_URL="https://archive.archlinux.org/packages/z/zlib/zlib-1%3A1.2.9-1-x86_64.pkg.tar.xz" \
	    && ZLIB_SHA256=bb0959c08c1735de27abf01440a6f8a17c5c51e61c3b4c707e988c906d3b7f67 \
	    && curl -Ls https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub -o /etc/apk/keys/sgerrand.rsa.pub \
	    && curl -Ls ${ALPINE_GLIBC_REPO}/${GLIBC_VER}/glibc-${GLIBC_VER}.apk > /tmp/${GLIBC_VER}.apk \
	    && apk add /tmp/${GLIBC_VER}.apk \
	    && curl -Ls ${GCC_LIBS_URL} -o /tmp/gcc-libs.tar.xz \
	    && echo "${GCC_LIBS_SHA256}  /tmp/gcc-libs.tar.xz" | sha256sum -c - \
	    && mkdir /tmp/gcc \
	    && tar -xf /tmp/gcc-libs.tar.xz -C /tmp/gcc \
	    && mv /tmp/gcc/usr/lib/libgcc* /tmp/gcc/usr/lib/libstdc++* /usr/glibc-compat/lib \
	    && strip /usr/glibc-compat/lib/libgcc_s.so.* /usr/glibc-compat/lib/libstdc++.so* \
	    && curl -Ls ${ZLIB_URL} -o /tmp/libz.tar.xz \
	    && echo "${ZLIB_SHA256}  /tmp/libz.tar.xz" | sha256sum -c - \
	    && mkdir /tmp/libz \
	    && tar -xf /tmp/libz.tar.xz -C /tmp/libz \
	    && mv /tmp/libz/usr/lib/libz.so* /usr/glibc-compat/lib \
	    && apk del binutils \
	    && rm -rf /tmp/${GLIBC_VER}.apk /tmp/gcc /tmp/gcc-libs.tar.xz /tmp/libz /tmp/libz.tar.xz /var/cache/apk/*

%else%
	# centos/redhat setup

%ifvar buildCommands%
# commands to be executed before install
	%loop buildCommands%
		%ifvar buildTarget equals('pre-install')%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
		%endif%
	%endloop%
%endif%

	%ifvar buildCommands%
# commands to be executed before install
	%loop buildCommands%
		%ifvar buildTarget equals('pre-build')%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
		%endif%
	%endloop%
%endif%

	RUN useradd -c 'sagadmin user' -m -d /home/sag -s /bin/sh sagadmin 

	#RUN mkdir /opt
	#RUN chown -R sagadmin /opt

%endif%

# files to be added to based image (includes configuration and package)
%ifvar /haveBuildCopy equals('true')%
	%loop buildCommands%
		%ifvar buildTarget equals('build')%
			%ifvar commandType equals('copy')%
# %value description%%nl%
				%ifvar /user%
					COPY --chown=%value /user% --from=INSTALLER %value source% %value target%
				%else%
					COPY --from=INSTALLER %value source% %value target%
				%endif%
			%endif%
		%endif%
	%endloop%
%else%
	%ifvar user%
		COPY --chown=%value /user% --from=INSTALLER /opt/softwareag /opt/softwareag
	%else%
		COPY --from=INSTALLER /opt/softwareag /opt/softwareag
	%endif%
%endif%

%ifvar env%
# Environment variables used by entrypoint and healthcheck
	%loop env%
ENV %value key% %value value%
	%endloop%
%endif%

ENV JAVA_HOME /opt/softwareag/jvm/jvm

%ifvar ports%
# define exposed ports
	%loop ports%
EXPOSE %value ports%
	%endloop%
%endif%

%ifvar volumes%
# volumes to be automatically defined when running container
%loop volumes%
	VOLUME %value volumes%
%endloop%
%endif%

%ifvar buildCommands%
# files to be added once installation completed
	%loop buildCommands%
		%ifvar buildTarget equals('build')%
			%ifvar commandType equals('file')%
# %value description%%nl%
				%ifvar /user%
					ADD --chown=%value /user% %value source% %value target% 
				%else%
					ADD %value source% %value target%
				%endif%
				%ifvar chmod -notempty%
					RUN chmod u+x %value chmod%
				%endif%
			%endif%
		%endif%
	%endloop%
%endif%
%nl%

user sagadmin

%ifvar buildCommands%
# commands to be executed after build
	%loop buildCommands%
		%ifvar buildTarget equals('build')%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
		%endif%
	%endloop%
%endif%

%ifvar entryPoint%
# script to run on startup (must be included in add files files above and include path!!)
	ENTRYPOINT ["%value entryPoint%"]
%endif%

%ifvar healthcheck%
# script to be executed periodically to check if runtime is okay (must be included in add files files above!!)
HEALTHCHECK --interval=%value healthcheck/interval%s --timeout=%value healthcheck/timeout%s --start-period=%value healthcheck/startPeriod%s --retries=%value healthcheck/retries% \
	CMD %value healthcheck/script%
%endif%

