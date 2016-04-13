#!/bin/bash
#. /etc/init.d/functions

NAVI_BIN=`dirname "$0"`
cd ${NAVI_BIN}
cd ..

NAVI_HOME=`pwd`
PIDS_DIR=${NAVI_HOME}/pids

function pre_kill_process() {
   cd ${PIDS_DIR}
   RESTATUS=0

   if [ ! $# -eq 0 ];then
      kill_process `cat $1.apid`
      rm -fr $1.apid
      return
   fi

   for PID_FILE in `ls *.apid`
    do
     PID=`cat ${PID_FILE}`
     kill_process ${PID}
     if [ ${RESTATUS} -eq 1 ];then
       continue
     fi
     rm -fr ${PID_FILE}
   done
}

function kill_process(){
   PID=$1
   
   if checkpid ${PID}; then
       kill ${PID} >/dev/null 2>&1
       if [ $? -eq 0 ];then
         echo "kill process ${PID} successfully!"
	 RESTATUS=0
       else
	 RESTATUS=1
         echo "kill process ${PID} failly!"
       fi
   else
       echo "$pid process isn't exist!"
   fi
}

pre_kill_process $1

exit ${RESTATUS}
