#!/bin/sh
#
#

status=`curl -s -o /dev/null -w "%{http_code}" wm-api-portal:18101`

cmdret=$?
if [[ $cmdret != 0 || "$status" != "306" ]]
then
  exit 1
else
  exit 0
fi

