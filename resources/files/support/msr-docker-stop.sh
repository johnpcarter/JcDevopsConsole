#!/bin/sh

echo "Stopped webMethods components......"

USER=`id -u -n`

if [ $USER == 'sagadmin' ]
then
	echo "Goodbye"
else
	echo "Goodbye"
fi