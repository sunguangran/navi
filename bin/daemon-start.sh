#!/bin/bash
if [ $# -lt 4 ];then
 echo "Pealse input the module name,the job name,the operation option and the job parameters."
 echo "Notice: the operation includes -start,-stop,-restart and -next.The job parameters's formation are"
 echo " key-value pairs like as 'rundata(date)=2013-01-22'."
 exit 0
fi
module_nm=$1;
job_nm=$2;

if [[ "$3" =~ ^\-* ]];then
  opt_option=$3;
else
  echo "the operation option should be -start,-stop,-restart or -next!"
  exit 0
fi


JAVA_NAVI_BIN=`dirname "$0"`
cd $JAVA_NAVI_BIN
cd ..
JAVA_NAVI_HOME=`pwd`
export JAVA_NAVI_HOME
cd bin
logs_dir=$JAVA_NAVI_HOME/logs

if [ ! -d "$logs_dir" ]
then
  mkdir "$logs_dir"
fi

pids_dir=$JAVA_NAVI_HOME/daemon-pids

if [ ! -d "$pids_dir" ]
then
  mkdir "$pids_dir"
fi

modules_dir=$JAVA_NAVI_HOME/NaviModules
if [ ! -d "$modules_dir" ]
then
  mkdir "$modules_dir"
fi

logs_dir=$logs_dir"/$module_nm-$4"
if [ ! -d "$logs_dir" ]
then
  mkdir "$logs_dir"
fi

log_path=$logs_dir"/$module_nm-$4.log"
jvm_options="-server -Xmx1024m -Xms1024m -XX:NewSize=384m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
jvm_args="-DJAVA_NAVI_HOME=$JAVA_NAVI_HOME -Dfile.encoding=UTF-8 -Dlog4j.configuration=log4j.configuration -Dfile.log.path=$log_path"
server_class="com.youku.java.navi.boot.NaviDaemonMain"
class_path=$JAVA_NAVI_HOME/NaviLibs/JavaNaviBoot-0.1.4-release.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-core-2.2.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-api-2.2.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-1.2-api-2.2.jar
exec "java" $jvm_options $jvm_args -cp $class_path $server_class $module_nm $job_nm $opt_option $4 2>$logs_dir/$module_nm-$4.err &
echo $!>$pids_dir/$module_nm-$4.pid
