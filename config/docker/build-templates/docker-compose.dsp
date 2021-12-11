version: '2'

services: %loop deployments%%loop containers%%ifvar active equals('true')%
  %value name%: 
%ifvar build/context -notempty%
    build:
       context: %value build/context%
    %ifvar build/dockerfile -notempty%
       dockerfile: %value build/dockerfile%%endif%
%ifvar haveEnvForCompose -notempty%
       args:
%loop build/buildCommands%
%ifvar commandType equals('env')%- %value source%=%value target%%endif%
%endloop%%endif%
%else%
    image: %value image%%endif%%nl%
%ifvar hostname -notempty%
    hostname: %value hostname%%else%
    hostname: %value name%
%endif%%ifvar ports[0]/external -notempty%
    ports: %loop ports%
        - "%value external%:%value internal%"%endloop%%endif%%ifvar volumes[0]/target -notempty%
    volumes: %loop volumes%
        - %value source%:%value target%
%endloop%%endif%%ifvar env[0]/source -notempty%
    environment: %loop env%
        - %value source%=%value target%
%endloop%%endif%%ifvar depends[0] -notempty%
    depends_on: %loop depends%
        - "%value depends%"%endloop%%endif%
  %endloop%
%endloop%
%endif%
%nl%
volumes:%loop deployments%%loop containers%%ifvar active equals('true')%%ifvar volumes%%loop volumes%     
   %ifvar source matches('\/*')%%else%%value source%:%endif%%endloop%%endif%%endloop%%endif%
%endloop%
