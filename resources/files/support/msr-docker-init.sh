#!/bin/sh
#
# startup script
#

USER=`id -u -n`

# Define SIGTERM-handler for graceful shutdown
term_handler() {
  if [ $childPID -ne 0 ]; then
    /bin/sh ./docker-stop.sh
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

# update env properties with overrides from properties file (if any)

if [ -r /opt/softwareag/IntegrationServer/application.properties ]
then
	port=`grep settings.watt.server.port /opt/softwareag/IntegrationServer/application.properties | awk -F "=" '{print $2}'`
	export port
fi

# start up components

if [ -r /usr/sbin/apachectl ]
then
# if installed, run as it will allow us to redirect 80 or 443 ports to msr

	if [ $USER == 'root' ]
	then
		echo "Starting apache2 (requires root) ........."

		/usr/sbin/apachectl
	else
		echo " ********************** Cannot start apache2, must run docker-init as root user ********************"
	fi
fi

echo "Starting webMethods components......"

USER=`id -u -n`

if [ $USER == 'sagadmin' ]
then
	/opt/softwareag/IntegrationServer/bin/startContainer.sh
else
	su sagadmin -c '/opt/softwareag/IntegrationServer/bin/startContainer.sh'
fi

# end
