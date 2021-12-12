#/bin/sh

export JAVA_HOME=/opt/softwareag/jvm/jvm; 

for file in $@
do
	echo "Merging ${file} into /opt/softwareag/IntegrationServer/config";

	if [ -r /opt/softwareag/IntegrationServer/config/$file ]
	then
		awk '{if ($1 == "<value") print $0}' /tmp/$file /opt/softwareag/IntegrationServer/config/$file | sort | uniq > /tmp/combined.txt
		echo "</Values>" >> /tmp/combined.txt
		echo "<Values version=\"2.0\">" > /opt/softwareag/IntegrationServer/config/$file
		cat /tmp/combined.txt >> /opt/softwareag/IntegrationServer/config/$file

		rm /tmp/combined.txt
	else

		# target file doesn't exist, just move tmp file into place.

		mv /tmp/$file /opt/softwareag/IntegrationServer/config/$file
	fi
	
done
