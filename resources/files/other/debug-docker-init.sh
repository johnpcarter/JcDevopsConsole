#!/bin/sh
#
# startup script
#


# Define SIGTERM-handler for graceful shutdown
term_handler() {
  if [ $childPID -ne 0 ]; then
    /bin/bash ./docker-stop.sh
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

# start up components

export DEBUG_PORT=10033

echo "Starting webMethods components with debug mode (${DEBUG_PORT}) ......"

#/opt/sag/profiles/SPM/bin/startup.sh

USER=`id -u -n`

if [ $USER == 'sagadmin' ]
then
  if [ "x${debug}" == 'xtrue' ]
  then
	  /opt/softwareag/profiles/IS_default/bin/startDebugMode.sh &
  else
    /opt/softwareag/profiles/IS_default/bin/startup.sh
  fi
else
  if [ "x${debug}" == 'xtrue' ]
  then
	  su sagadmin -c '/opt/softwareag/profiles/IS_default/bin/startDebugMode.sh &'
  else
    su sagadmin -c '/opt/softwareag/profiles/IS_default/bin/startup.sh'
  fi
fi

# keep container running

ALERT_LOG=/opt/softwareag/IntegrationServer/instances/default/logs/server.log

while [ ! -r $ALERT_LOG ]
do
  echo "waiting for server.log to start..."
  sleep 10
done

echo "outputing server.log to stdout...."

tail -f $ALERT_LOG &
childPID=$!
wait $childPID

# end
