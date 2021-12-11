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

USER=`id -u -n`

if [ "x${INSTANCE_NAME}" == 'x' ]
then
	realm="umserver"
fi

echo "Starting webMethods um realm ${INSTANCE_NAME} ......"

if [ $USER == 'sagadmin' ]
then
	cd /opt/softwareag/UniversalMessaging/server/${realm}/bin
	./nserver
else
	su sagadmin -c '/opt/softwareag/UniversalMessaging/server/${realm}/bin;./nserver'
fi

# end
