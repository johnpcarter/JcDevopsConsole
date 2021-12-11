#!/bin/sh
#
#

if [ x${apigw_port} == 'x' ]
then
		apigw_port=4485
fi

status=`curl -s -o /dev/null -w "%{http_code}" http://localhost:${apigw_port}/rest/microgateway/status`

cmdret=$?
if [[ $cmdret != 0 || "$status" != "200" ]]
then
  exit 1
else
  exit 0
fi
