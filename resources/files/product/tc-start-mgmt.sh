#!/usr/bin/env sh

# Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
# Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.

BIN_DIR=`dirname $0`

# Initialize vars if they weren't provided by the config file
[[ -z "$TMC_HOME" ]] && export TMC_HOME=`cd $BIN_DIR/..;pwd`
[[ -z "$JAVA_OPTS" ]] && export JAVA_OPTS="-Xmx1024m -XX:MaxDirectMemorySize=1024g"

export APP_NAME=tmc
export RUN_ARGS="--spring.config.location=$TMC_HOME/conf/tmc.properties"

# this will only happen if using sag installer
#if [ -r "${BIN_DIR}/setenv.sh" ] ; then
#  . "${BIN_DIR}/setenv.sh"
#fi

if [ ! -d "$JAVA_HOME" ]; then
   echo "ERROR: JAVA_HOME must point to Java installation."
   echo "    $JAVA_HOME"
   exit 2
fi

JAVA="$JAVA_HOME/bin/java"

exec "$JAVA" $JAVA_OPTS -jar $TMC_HOME/lib/tms.jar $RUN_ARGS