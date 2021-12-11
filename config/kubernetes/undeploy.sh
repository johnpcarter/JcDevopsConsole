#!/bin/sh

%loop deployments%
kubectl delete -n %value /namespace% statefulset %value deployments%
%endloop%

%loop services%
kubectl delete -n %value /namespace% service %value services%
%endloop%

%loop volumes%
kubectl delete pvc -n %value /namespace% %value volumes%%nl%
#kubectl delete pv %value volumes%
%endloop%

kubectl delete namespace %value namespace%

%ifvar toImage -notnull%
docker image rm -f %value toImage%
%endif%