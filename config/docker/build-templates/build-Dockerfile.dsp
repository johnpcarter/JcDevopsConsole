#
# Template to customise msc based image
#

FROM %value fromImage%

LABEL MAINTAINER="%value author%" \
	DESCRIPTION="%value description%" \
	COMMENT="%value buildComments%" \
	CUSTOM="true" \
	SAG="true" \
	BUILD=%value id% \
	BUILD-TEMPLATE="%value buildTemplate%" \
	TYPE="%value type%" %ifvar args/primaryPort -notempty%\%nl%
	PRIMARY_PORT="%value args/primaryPort%" %endifvar%

#user root

%ifvar ports%
# define exposed ports
	%loop ports%
EXPOSE %value ports%
	%endloop%
%endifvar%

%ifvar args/primaryPort -notempty%\%nl%
ENV port %value args/primaryPort%  %endifvar%

%ifvar buildUser%
# user to be used when running scripts
USER %value buildUser%
%endifvar%

%ifvar volumes%
# volumes to be automatically defined when running container
%loop volumes%
VOLUME %value volumes%
%endloop%
%endifvar%

%ifvar buildCommands%
# files to be added to based image (includes configuration and package)
	%loop buildCommands%
%ifvar commandType equals('file')%
	%ifvar /user%
	ADD --chown=%value /user% %value source% %value target%
	%else%
	ADD %value source% %value target%
	%endifvar%
	%ifvar chmod -notempty%
		RUN chmod u+x %value chmod%
	%endif%
%endif%
	%endloop%
%endifvar%
%ifvar env[0]/key -notempty%
# Environment variables used by entrypoint and healthcheck
	%loop env%
ENV %value key% %value value%
	%endloop%
%endifvar%

%ifvar buildCommands%
# Scripts to be executed during build (must be included in add files files above and include path!!)
	%loop buildCommands%
		%ifvar commandType equals('run')%
RUN %value target%
		%endif%
	%endloop%
%endifvar%

%ifvar entryPoint%
# script to run on startup (must be included in add files files above and include path!!)
ENTRYPOINT ["%value entryPoint%"]
%endifvar%
%ifvar healthcheck/script%
# script to be executed periodically to check if runtime is okay (must be included in add files files above!!)
HEALTHCHECK --interval=%value healthcheck/interval%s --timeout=%value healthcheck/timeout%s --start-period=%value healthcheck/startPeriod%s --retries=%value healthcheck/retries% \
	CMD %value healthcheck/script%
%endifvar%

%ifvar entryUser -notempty%
# User to be used when starting container
USER %value entryUser%
%endif%
