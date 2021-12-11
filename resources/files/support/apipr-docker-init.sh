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

echo "API Portal......"
/opt/softwareag/API_Portal/server/bin/CloudAgentApp.sh start

sleep 5

echo "outputing wrapper.log to stdout...."

ALERT_LOG=/opt/softwareag/API_Portal/server/logs/CloudAgent.log
tail -f $ALERT_LOG &
childPID=$!
wait $childPID

# end
