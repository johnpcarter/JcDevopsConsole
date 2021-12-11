#!/bin/sh
#
#

on_healthy() {
  echo "We are up and running...";

  ## add your post processing here..... 
}

if [ "x${port}" == "x" ]
then
	port=5555
fi

status=`curl -s -o /dev/null -w "%{http_code}" http://localhost:${port}/invoke/wm.server/ping`

cmdret=$?
if [[ $cmdret != 0 || "$status" != "200" ]]
then
	# bad

  exit 1
else
	# good

	if [ ! -e /tmp/healthy.txt ]
	then
		touch /tmp/healthy.txt;

		# only called after first health check determines all is good

		on_healthy;
	fi

  exit 0
fi
