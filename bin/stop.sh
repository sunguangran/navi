#!/bin/bash
. /etc/init.d/functions

NAVI_BIN=`dirname "$0"`
cd ${NAVI_BIN}
cd ..

NAVI_HOME=`pwd`
PIDS_DIR=${NAVI_HOME}/pids

function pre_kill_process() {
   cd ${PIDS_DIR}
   restatus=0

   if [ ! $# -eq 0 ];then
      kill_process `cat $1.pid`
      return
   fi

   for pid_file in `ls *.pid`
    do
     pid=`cat ${pid_file}`
     kill_process ${pid}
     if [ ${restatus} -eq 1 ];then
       continue
     fi
     rm -fr ${pid_file}
   done
}

function kill_process() {
   pid=$1
   
   if checkpid ${pid};then
       kill ${pid} >/dev/null 2>&1
       sleep 3
       if test $( ps -p ${pid} | wc -l ) -eq 1; then
         echo "kill process $pid successfully!"
	     restatus=0
       else
	     restatus=1
         echo "kill process $pid failed!"
       fi
   else
       echo "$pid process doesn't exist!"
   fi
}

pre_kill_process $1
exit ${restatus}
