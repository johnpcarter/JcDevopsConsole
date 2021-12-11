#!/bin/sh

# Define SIGTERM-handler for graceful shutdown
term_handler() {
  if [ $childPID -ne 0 ]; then
  	echo "SAG - Shutting down database...."
    mysqladmin -u root -pmanage shutdown
  fi
  exit 143; # 128 + 15 -- SIGTERM
}
# setup SIGTERM Handler
trap 'kill ${!}; term_handler' SIGTERM

#MYSQL_ROOT_HOST=%;export MYSQL_ROOT;
#MYSQL_ROOT_PASSWORD=manage;export MYSQL_ROOT_PASSWORD;

echo "root password is ${MYSQL_ROOT_PASSWORD}"

echo "SAG - mysql with webMethods Schema"

/entrypoint.sh mysqld &

sleep 30

STAT=`mysql -u "root" "-pmanage" -sse "select count(*) from COMPONENT_EVENT;" wm`
cmdret=$?

echo "SAG - wm tables stat '${cmdret}'"

if [ "x$cmdret" == "x0" ]
then
   	echo "SAG - webMethods tables found"
else
	echo "====================================================="
    echo "SAG - webMethods tables not found, setting up db 'wm'"

	mysql -u "root" "-pmanage" < /sag/init-mysql.sql
	echo "SAG - running db scripts"

	for script in `find /sag/scripts -name '*.sql' -print0 | sort -z | xargs -r0`
	do
		echo ">>>>>>>>>>>>>>>>>>>>>>>>> SAG - running script ${script}"
		mysql -u "wm" "-pmanage" wm < ${script}
	done

	# Add your own custom scripts here.

fi

echo "SAG - outputing mysqld.log to stdout...."

#ALERT_LOG=/var/log/mysql/error.log
ALERT_LOG=/var/log/mysqld.log

tail -f $ALERT_LOG &
childPID=$!
wait $childPID