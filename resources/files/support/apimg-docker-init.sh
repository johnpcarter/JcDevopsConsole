#!/bin/sh

term_handler() {
  if [ $childPID -ne 0 ]; then
    /bin/sh ./docker-stop.sh
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

# start up components

if [ x${mcgw_ports_http} == 'x' ]
then
		mcgw_ports_http=4485
fi

if [ x${mcgw_ports_https} == 'x' ]
then
		mcgw_ports_https=4486
fi

# try to interrogate local micro-service runtime to find out what API's we should be securing

if [ x${api_server_url} == 'x' ]
then
	api_server_url="http://localhost:5555"
fi

status=`curl -s -o /dev/null -w "%{http_code}" ${api_server_url}/invoke/wm.server/ping`
cmdret=$?
try=0;
while [[ "${cmdret}" != "0" && "$try" -lt 12 ]]
do
  echo "Waiting (10s) for API container at '${api_server_url}' to start... " + ${cmdret}
  sleep 10
 
  echo "Checking for local server availability after ${try} attempts"

  status=`curl -s -o /dev/null -w "%{http_code}" ${api_server_url}/invoke/wm.server/ping`
  cmdret=$?
  try=$((try+1))
done

api_path="rad/jc.api:api/tools/local"

case ${api_server_url} in
 */) url="${api_server_url}${api_path}";;
 *) url="${api_server_url}/${api_path}";;
esac

if [ $status -eq 200 ]
then
	
	if [ x"${mcgw_downloads_apis}" = "x" ]
	then
		echo "Will fetch API's via ${url}"

		if [ x${api_server_user} == 'x' ]
		then	# service should be anonymous anyhows
			mcgw_downloads_apis=`curl -s ${url}`
		else
			mcgw_downloads_apis=`curl -s --user ${api_server_user}:${api_server_password} ${url}`
		fi
	fi
else
	echo "Environment specified API's: '${mcgw_downloads_apis}'"
fi

cd /opt/softwareag/Microgateway;

if [ "x${mcgw_api_gateway_url}" != 'x' ]
then

	run="./microgateway.sh start -ua delegated -p ${mcgw_ports_http} -gw ${mcgw_api_gateway_url} -gwu ${mcgw_api_gateway_user} -gwp ${mcgw_api_gateway_password}"

	if [[ "x${mcgw_downloads_apis}" != 'x' && "x${mcgw_downloads_apis}" != "xnull" ]]
	then
		run="${run} -apis ${mcgw_downloads_apis}"
	else
		echo "No API's have been provided, either by local Micro Service Runtime, nor via environment variable mcgw_downloads_apis, exiting..."
		exit 1;
	fi

	if [ "x${mcgw_downloads_policies}" != 'x' ]
	then
		run="${run} -pols '${mcgw_downloads_policies}'"
	fi

	if [ "x${mcgw_logging_level}" != 'x' ]
	then
		run="${run} -lv '${lmcgw_logging_level}'"
	fi

	if [ "x${mcgw_downloads_applications}" != 'x' ]
	then
		run="${run} -apps ${mcgw_downloads_applications}"
	fi

	if [ "x${mcgw_custom_settings}" != 'x' ]
	then
		run="${run} -c /opt/softwareag/Microgateway/config/${mcgw_custom_settings}"
	fi

	echo "Starting API Micro Gateway with command: ${run}";

	cmdret=x
	while [ "${cmdret}" != "0" ]
	do
		if [ "$cmdret" != "x" ]
		then
			echo "Failed to connect to API Gateway, will try again in 10 seconds"
			sleep 10;
		fi

		eval $run
		cmdret=$?
	done 
else

	if [ "x${mcgw_archive_file}" == 'x' ]
	then
		mcgw_archive_file="/apigw-archive.zip"
	fi

	echo "Starting API Gateway on port '${mcgw_ports_http}' with archive ${mcgw_archive_file}"

	./microgateway.sh start -p ${mcgw_ports_http} --archive ${mcgw_archive_file}
fi

# keep container running

sleep 5

echo "outputing wrapper.log to stdout...."

ALERT_LOG=/opt/softwareag/Microgateway/logs/gw.${mcgw_ports_http}.out
tail -f $ALERT_LOG &
childPID=$!
wait $childPID

# end
