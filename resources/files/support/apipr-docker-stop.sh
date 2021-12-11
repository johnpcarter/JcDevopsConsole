#!/bin/sh

echo "Stopping webMethods components......"

#/opt/sag/profiles/IS_default/bin/shutdown.sh

cd /opt/softwareag/API_Portal/server

#su wm -c './stop-api.sh'
/opt/softwareag/API_Portal/server/bin/CloudAgentApp.sh stop
