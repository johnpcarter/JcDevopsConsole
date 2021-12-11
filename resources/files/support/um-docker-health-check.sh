#!/bin/sh
#
#

on_healthy() {
  echo "We are up and running...";

  ## add your post processing here..... 
}

if [ "x${port}" == "x" ]
then
	port="9000"
fi

cd /opt/softwareag/UniversalMessaging/tools/runner
./runUMTool.sh HealthChecker -rname=nsp://localhost:${port}

cmdret=$?

if [ $cmdret != 0 ]
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
