#!/bin/sh

echo "Stopping webMethods components......"

USER=`id -u -n`

if [ $USER == 'wm' ]
then
	/opt/softwareag/profiles/MWS_default/bin/shutdown.sh
else
	su sagadmin -c '/opt/softwareag/profiles/MWS_default/bin/shutdown.sh'
fi