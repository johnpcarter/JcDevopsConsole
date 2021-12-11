#!/usr/bin/env sh

# Copyright (c) 2011-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA, USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
# Use, reproduction, transfer, publication or disclosure is prohibited except as specifically provided for in your License Agreement with Software AG.

BIN_DIR=`dirname $0`

# Initialize vars if they weren't provided by the config file
[[ -z "$TMC_HOME" ]] && export TMC_HOME=`cd $BIN_DIR/..;pwd`


PID=$(cat $TMC_HOME/run/tmc.pid)