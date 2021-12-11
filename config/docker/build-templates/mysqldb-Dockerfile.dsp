FROM %value dockerBuildProperties/fromImage%

LABEL MAINTAINER="%value author%" \
	DESCRIPTION="%value description%" \
	COMMENT="%value buildComments%" \
	CUSTOM="false" \
	SAG="false" \
	TYPE="%value type%" \
	BUILD="%value id%" %ifvar args/primaryPort -notempty%\%nl%
	PRIMARY_PORT="%value args/primaryPort%" %endifvar%

%ifvar buildCommands -notempty%
# files to be added once installation completed
	%loop buildCommands%
			%ifvar commandType equals('file')%
# %value description%%nl%
				%ifvar /user%
					COPY --chown=%value /user% %value source% %value target% 
				%else%
					COPY %value source% %value target%
				%endif%
				%ifvar chmod -notempty%
					RUN chmod u+x %value chmod%
				%endif%
			%endif%
	%endloop%
%endif%
%nl%

%ifvar buildCommands -notempty%
# commands to be executed after installation but before build
	%loop buildCommands%
			%ifvar commandType equals('run')%
# %value description%%nl%
				run %value target%
			%endif%
	%endloop%
%endif%

VOLUME /var/lib/mysql

%ifvar dockerBuildProperties/entryUser -notempty%
	ENTRYPOINT ["%value dockerBuildProperties/entryUser%"]
%endif%