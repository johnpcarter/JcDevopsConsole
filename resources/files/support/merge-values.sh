#/bin/sh

export JAVA_HOME=/opt/softwareag/jvm/jvm; 

if [ -r /opt/softwareag/IntegrationServer/instances/default ] 
then
	SAG_HOME=/opt/softwareag/IntegrationServer/instances/default
else 
	SAG_HOME=/opt/softwareag/IntegrationServer
fi

for file in $@
do
	echo "Merging ${file} into /opt/softwareag/IntegrationServer/config";

	if [ -r ${SAG_HOME}/config/$file ]
	then
		awk '{if ($1 == "<value") print $0}' /tmp/$file ${SAG_HOME}/config/$file | sort | uniq > /tmp/combined.txt
		echo "</Values>" >> /tmp/combined.txt
		echo "<Values version=\"2.0\">" > ${SAG_HOME}/config/$file
		cat /tmp/combined.txt >> ${SAG_HOME}/config/$file

		rm /tmp/combined.txt
	else

		# target file doesn't exist, just move tmp file into place.

		mv /tmp/$file ${SAG_HOME}/config/$file
	fi
	
done
