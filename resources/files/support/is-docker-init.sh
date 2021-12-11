#!/bin/sh
#
# startup script
#


# Define SIGTERM-handler for graceful shutdown
term_handler() {
  if [ $childPID -ne 0 ]; then
    /bin/sh ./docker-stop.sh
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

# start up components

echo "Starting webMethods components......"

USER=`id -u -n`

if [ $USER == 'sagadmin' ]
then
	/opt/softwareag/IntegrationServer/bin/startContainer.sh
else
	su sagadmin -c '/opt/softwareag/IntegrationServer/bin/startContainer.sh'
fi

# end
