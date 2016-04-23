#!/bin/bash
#. /etc/init.d/functions

NAVI_BIN=`dirname "$0"`
cd ${NAVI_BIN}
cd ..
NAVI_HOME=`pwd`

LOGSDIR="${NAVI_HOME}/logs"
if [ ! -d "${LOGSDIR}" ]
then
  mkdir "${LOGSDIR}"
fi

PIDSDIR="${NAVI_HOME}/pids"
if [ ! -d "${PIDSDIR}" ]
then
  mkdir "${PIDSDIR}"
fi

MODULESDIR="${NAVI_HOME}/modules"
if [ ! -d "${MODULESDIR}" ]
then
  mkdir "${MODULESDIR}"
fi

JVMOPTS="-server -Xmx1024m -Xms1024m -XX:NewSize=384m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-DNAVI_HOME=${NAVI_HOME} -Dfile.encoding=UTF-8 "

#JMX debug --  attach to eclipse,eclipse start first ---debug startup
#JVMARGS="$JVMARGS -agentlib:jdwp=transport=dt_socket,suspend=y,address=10.10.105.54:9000"
#JMX debug --  eclipse connect to server,server start first
#JVMARGS="$JVMARGS -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n"

#JProfile
#JVMARGS="$JVMARGS -agentpath:/opt/jprofiler8/bin/linux-x64/libjprofilerti.so=port=8849"

#JVM Monitor
#JVMARGS="$JVMARGS -Dcom.sun.management.jmxremote.port=8888 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

MAINCLASS="com.youku.java.navi.boot.NaviMain"
BOOTPATH="${NAVI_HOME}/libs/bootstrap-1.0.0-release.jar:${NAVI_HOME}/libs/logback-core-1.1.2.jar:${NAVI_HOME}/libs/logback-classic-1.1.2.jar:${NAVI_HOME}/libs/logback-access-1.1.2.jar:${NAVI_HOME}/libs/slf4j-api-1.7.5.jar:${NAVI_HOME}/libs/slf4j-log4j12-1.7.5.jar:${NAVI_HOME}/libs/commons-lang-2.6.jar"
CONFIGPATH="${NAVI_HOME}/conf/server.conf"

if [ $# -eq 0 ]; then
   PORT=$(egrep "^listen.port=" ${CONFIGPATH} | awk -F"=" '{print $2}')
   DEFAULT_LOG_DIR="${LOGSDIR}/${PORT}"

   if [ ! -d "${DEFAULT_LOG_DIR}" ]; then
     mkdir "${DEFAULT_LOG_DIR}"
   fi

   FILE_LOG_DIR="${DEFAULT_LOG_DIR}/${PORT}"
   ERROR_LOG_DIR="${DEFAULT_LOG_DIR}/${PORT}.err"
   PID_FILE="${PIDSDIR}/${PORT}.pid"
   JVMARGS="$JVMARGS -Dfile.log.path=$FILE_LOG_DIR"
else
   proc_log_dir="$LOGSDIR/$1"

   if [ ! -d "$proc_log_dir" ]
    then
      mkdir "$proc_log_dir"
    fi

   FILE_LOG_DIR="$proc_log_dir/$1"
   ERROR_LOG_DIR="$proc_log_dir/$1.err"
   PID_FILE="${PIDSDIR}/$1.pid"
   JVMARGS="$JVMARGS -Dfile.log.path=$FILE_LOG_DIR -Dlisten.port=$1"
fi

nohup "java" ${JVMOPTS} ${JVMARGS} -cp ${BOOTPATH} ${MAINCLASS}   2>${ERROR_LOG_DIR} &
if [ $? -eq 0 ]; then
   pid=$!
   if test $( ps -p ${pid} | wc -l ) -ne 1
   then
     echo ${pid} > ${PID_FILE}
     echo "navi server process $pid has been started successfully.";
     exit 0
   else
     echo "navi server can't be started, please see the error log!";
     exit 1;
   fi
else 
   echo "navi server can't be started!,please see the error log";
   exit 1
fi
