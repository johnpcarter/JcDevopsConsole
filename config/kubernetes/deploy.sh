#!/bin/sh

%ifvar buildCommand -notnull%
%value buildCommand%
%endif%

if [ -r namespace.yaml ]
then
	kubectl create -f namespace.yaml
fi

%loop volumes%
kubectl create -f pvc/%value volumes%
%endloop%

%loop deployments%
kubectl create -f %value deployments%
%endloop%

%loop services%
kubectl create -f %value services%
%endloop%