#
# Template to customise msc based image
#

FROM store/softwareag/webmethods-microservicesruntime:10.11.0.0-slim
LABEL MAINTAINER="" \
	DESCRIPTION="" \
	COMMENT="upgraded based to MSR 10.11, many other changes to ux " \
	CUSTOM="true" \
	SAG="true" \
	BUILD=build-msc-00070 \
	BUILD-TEMPLATE="devops-console" \
	TYPE="Micro Service" 
#user root


# define exposed ports
	
EXPOSE 5555	
EXPOSE 9999	


# user to be used when running scripts
USER root


# files to be added to based image (includes configuration and package)
	

	
	ADD --chown=sagadmin ./source/%srcDirForConfig%/aclmap_sm.cnf /tmp			

	
	ADD --chown=sagadmin ./support/merge-values.sh /tmp		
		RUN chmod u+x /tmp/merge-values.sh		
	

	
	ADD --chown=sagadmin ./resources/devops.properties /opt/softwareag/IntegrationServer/application.properties			
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	ADD --chown=sagadmin ./resources/devops_home.html /opt/softwareag/IntegrationServer/packages/WmAdmin/pub/index.html			

	
	ADD --chown=sagadmin ./source/%srcDirForConfig%/globalVariables.cnf /opt/softwareag/IntegrationServer/config			

	
	ADD --chown=sagadmin ./resources/0000497888_MicroservicesRuntime_100.xml /opt/softwareag/IntegrationServer/config/licenseKey.xml			
	

	
	ADD --chown=sagadmin ./resources/devops_home.html /opt/softwareag/IntegrationServer/packages/WmRoot/pub/index.html				

	
	ADD --chown=sagadmin ./source/%srcDirForConfig%/acllist.cnf /tmp			

	
	ADD --chown=sagadmin ./source/%srcDirForConfig%/aclmap_sm.cnf /tmp			

	
	ADD --chown=sagadmin ./support/merge-values.sh /tmp		
		RUN chmod u+x /tmp/merge-values.sh		
	

	
	ADD --chown=sagadmin ./source/JcDevopsConsole /opt/softwareag/IntegrationServer/packages/JcDevopsConsole			

	
	ADD --chown=sagadmin ./source/JcAPITools /opt/softwareag/IntegrationServer/packages/JcAPITools			

	
	ADD --chown=sagadmin ./source/JcPublicTools /opt/softwareag/IntegrationServer/packages/JcPublicTools			

# Scripts to be executed during build (must be included in add files files above and include path!!)
	
			
			
		
RUN /tmp/merge-values.sh acllist.cnf aclmap_sm.cnf			
			
		
RUN mkdir -p /home/sag/devops			
		
RUN mkdir -p /home/sag/devops/configuration			
		
RUN mkdir -p /home/sag/devops/files			
		
RUN mkdir -p /home/sag/devops/files/installer-images			
		
RUN mkdir -p /home/sag/devops/files/licenses			
		
RUN mkdir -p /home/sag/devops/files/properties			
		
RUN mkdir -p /home/sag/devops/files/other			
		
RUN mkdir -p /home/sag/devops/configuration/build			
		
RUN mkdir -p /home/sag/devops/configuration/deployments			
		
RUN mkdir -p /home/sag/devops/configuration/install			
		
RUN mkdir -p /home/sag/devops/configuration/projects			
		
RUN mkdir -p /home/sag/devops/configuration/runs			
		
RUN mkdir -p /home/sag/devops/configuration/user			
			
			
			
		
RUN chown -R sagadmin:sagadmin /home/sag			
			
			
			
			
		
RUN /tmp/merge-values.sh acllist.cnf aclmap_sm.cnf			
			
			
			


# User to be used when starting container
USER root