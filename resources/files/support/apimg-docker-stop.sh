#!/bin/sh

if [ x${apigw_port} == 'x' ]
then
		apigw_port=4485
fi

cd /opt/softwareag/Microgateway;
./microgateway.sh stop -p ${apigw_port};