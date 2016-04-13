#!/bin/bash
#. /etc/init.d/functions

NAVI_BIN=`dirname "$0"`
cd ${NAVI_BIN}
cd ..

NAVI_HOME=`pwd`

LOGSDIR="${NAVI_HOME}/logs"
if [ ! -d "$LOGSDIR" ]
then
  mkdir "$LOGSDIR"
fi

PIDSDIR="${NAVI_HOME}/pids"
if [ ! -d "$PIDSDIR" ]
then
  mkdir "$PIDSDIR"
fi

MODULESDIR="${NAVI_HOME}/NaviModules"
if [ ! -d "$MODULESDIR" ]
then
  mkdir "$MODULESDIR"
fi

if [ $# -eq 1 ]
then
	logFile=$1
else
	logFile=async
fi


JVMOPTS="-server -Xmx1024m -Xms1024m -XX:NewSize=384m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-DJAVA_NAVI_HOME=${NAVI_HOME} -Dfile.encoding=UTF-8 "

#JMX debug --  attach to eclipse,eclipse start first ---debug startup
#JVMARGS="${JVMARGS} -agentlib:jdwp=transport=dt_socket,suspend=y,address=10.10.105.54:9000"
#JMX debug --  eclipse connect to server,server start first
#JVMARGS="${JVMARGS} -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"

#JProfile
#JVMARGS="${JVMARGS} -agentpath:/opt/youku/jprofiler8/bin/linux-x64/libjprofilerti.so=port=8849"

#JVM Monitor
#JVMARGS="${JVMARGS} -Dcom.sun.management.jmxremote.port=8888 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"


MAINCLASS="com.youku.java.navi.boot.NaviAsyncMain"
BOOTPATH="${NAVI_HOME}/libs/bootstrap-1.0.0-release.jar:${NAVI_HOME}/libs/logback-core-1.1.2.jar:${NAVI_HOME}/libs/logback-classic-1.1.2.jar:${NAVI_HOME}/libs/logback-access-1.1.2.jar:${NAVI_HOME}/libs/slf4j-api-1.7.5.jar:${NAVI_HOME}/libs/slf4j-log4j12-1.7.5.jar:${NAVI_HOME}/libs/commons-lang-2.6.jar"
CONFIGPATH="${NAVI_HOME}/conf/server.conf"

default_log_dir="$LOGSDIR/$logFile"
   
if [ ! -d "$default_log_dir" ]
then
	mkdir "$default_log_dir"
fi

FILE_LOG_PATH="$default_log_dir/$logFile.log"
ERR_LOG_PATH="$default_log_dir/$logFile.err"
PID_PATH="$PIDSDIR/$logFile.apid"
JVMARGS="${JVMARGS} -Dfile.log.path=${FILE_LOG_PATH}"

nohup "java" ${JVMOPTS} ${JVMARGS} -cp ${BOOTPATH} ${MAINCLASS} 2>${ERR_LOG_PATH} &
if [ $? -eq 0 ]; then
   PID=$!
   if test $( ps -p ${PID} | wc -l ) -ne 0
   then
     echo ${PID} > ${PID_PATH}
     echo "JavaNavi async Server has been started successfully!";
     exit 0
   else
     echo "JavaNavi async Server can't be started,please see the error log!";
     exit 1;
   fi
else 
   echo "JavaNavi async Server can't be started!,please see the error log";
   exit 1
fi
