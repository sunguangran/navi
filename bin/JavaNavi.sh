#!/bin/sh
. /etc/init.d/functions

NAVIUSER=navi
NAVI_HOME=/opt/navi/javanavi
LOGSDIR=/var/log/javanavi
PIDSDIR=/var/lib/javanavi/run

NAVI_BIN=`dirname "$0"`
cd ${NAVI_BIN}
cd ..
CURRENT_HOME=`pwd`

if [ "$NAVI_BIN" != "/etc/init.d" ];then
	NAVIUSER=`whoami`
	NAVI_HOME=${CURRENT_HOME}
	LOGSDIR="$NAVI_HOME/logs"
	PIDSDIR="$NAVI_HOME/pids"
else
	chown ${NAVIUSER} "${NAVI_HOME}" || exit $?
	chown ${NAVIUSER} "${LOGSDIR}" || exit $?
	chown ${NAVIUSER} "${PIDSDIR}" || exit $?
fi

if [ ! -d "$NAVI_HOME" ]
then
	echo "Can't find the JavaNavi home!"
	exit 1
fi

if [ ! -d "$LOGSDIR" ]
then
  mkdir -p "$LOGSDIR"
fi

if [ ! -d "${PIDSDIR}" ]
then
  mkdir -p "${PIDSDIR}"
fi

echo "NAVI_HOME: $NAVI_HOME"
echo "LOGDIR: $LOGSDIR"
echo "PIDSDIR: ${PIDSDIR}"
echo "NAVIUSER: $NAVIUSER"

MODULESDIR="$NAVI_HOME/modules"

JVMOPTS="-server -Xmx1024m -Xms1024m -XX:NewSize=384m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-DNAVI_HOME=$NAVI_HOME -Dfile.encoding=UTF-8 -Dlog4j.configuration=log4j.configuration"
NAVIMAINCLASS="com.youku.java.navi.boot.NaviMain"
DAEMONMAINCLASS="com.youku.java.navi.boot.NaviDaemonMain"

BOOTPATH="${NAVI_HOME}/libs/bootstrap-1.0.0-release.jar:${NAVI_HOME}/libs/logback-core-1.1.2.jar:${NAVI_HOME}/libs/logback-classic-1.1.2.jar:${NAVI_HOME}/libs/logback-access-1.1.2.jar:${NAVI_HOME}/libs/slf4j-api-1.7.5.jar:${NAVI_HOME}/libs/slf4j-log4j12-1.7.5.jar:${NAVI_HOME}/libs/commons-lang-2.6.jar"
CONFIGPATH="${NAVI_HOME}/conf/server.conf"

function pre_kill_process()
{
	cd ${PIDSDIR}
	RESTATUS=0
	if [ ! $# -eq 0 ];then
		echo "The javanavi server with port $1 is stopping..."

		PID_FILE=$1.pid
		if [ ! -f "${PID}_FILE}" ];then
			echo "Can't find the pid file, stop JavaNavi Server failed!"
			RESTATUS=1
			return
		fi

		kill_process `cat $1.pid`
		if [ ${RESTATUS} -eq 0 ];then
			rm ${PID_FILE}
		fi

		return
	fi

	for PID_FILE in `ls .`
	do
		PID=`cat ${PID_FILE}`
		PORT=`echo ${PID_FILE} | cut -d"." -f1`
		echo "The javanavi server with port ${PORT} is stopping..."
		kill_process ${PID}
		if [ ${RESTATUS} -eq 1 ];then
			continue
		fi
		rm ${PID_FILE}
	done
}

function kill_process(){
	PID=$1
	if checkpid ${PID}; then
		kill ${PID} > /dev/null 2>&1
		if [ $? -eq 0 ];then
			echo "Killed process ${PID}, stop JavaNavi Server successed!"
			RESTATUS=0
		else
			RESTATUS=1
			echo "Can't kill process ${PID}, stop JavaNavi Server failed!"
		fi
	else
		echo "process ${PID} doesn't exist, stop javanavi server failed!"
	fi
}

function getPort_array(){
	PORTS=`egrep ^listen.PORT= ${CONFIGPATH} | cut -d"=" -f2`
    OLD_IFS=$IFS
    IFS=","
    PORT_ARRAY=(${PORTS})
    IFS=${OLD_IFS}
}

function findPort(){
	CANFINDPORT=`netstat -vatn|grep LISTEN|grep $1|wc -l`
}

function start(){
	if [ $# -eq 0 ];then
		getPort_array
		for PORT in ${PORT_ARRAY[@]}
		do
			start_navi ${PORT}
		done
	else
		start_navi $1
	fi	
}

function start_navi(){
	PORT=$1
	findPort ${PORT}

	if [ ${CANFINDPORT} -ne 0 ];then
		echo "The PORT $PORT has already used! Start JavaNavi Server with PORT $PORT failed!"
		return
	fi

	default_log_dir="$LOGSDIR/$PORT"

	if [ ! -d "$default_log_dir" ]
	then
		su -l ${NAVIUSER} -c "mkdir $default_log_dir"
	fi

	file_log_path="$default_log_dir/$PORT"
	error_log_path="$default_log_dir/$PORT.err"
	pid_path="${PID}SDIR/$PORT.pid"
	NEWJVMARGS="$JVMARGS -Dfile.log.path=$file_log_path -Dlisten.PORT=$PORT"

	NAVI_TEMP_FILE=${NAVI_HOME}/bin/navi_temp.sh

	echo "JavaNavi Server with PORT ${PORT} is starting..."
	
	echo "exec java $JVMOPTS $NEWJVMARGS -cp $BOOTPATH $NAVIMAINCLASS  2>$error_log_path &" > ${NAVI_TEMP_FILE}
	echo "echo \$! > ${PID}_path" >> ${NAVI_TEMP_FILE}
	echo "exit \$?" >> ${NAVI_TEMP_FILE}
	
	su -l ${NAVIUSER} -c "sh $NAVI_TEMP_FILE"
	
	if [ $? -ne 0 ];then
		echo "Start JavaNavi Server with PORT $PORT failed! Please check the logs!"
		rm ${PID}_path
		rm ${NAVI_TEMP_FILE}
		return
	fi

	rm ${NAVI_TEMP_FILE}

	findPort ${PORT}
	nbSeconds=3

	while [ ${CANFINDPORT} -eq 0 ] && [ ${nbSeconds} -lt 5 ];do
		sleep 1
		let "nbSeconds = $nbSeconds + 1"
		findPort ${PORT}
	done
	
	if [ ${CANFINDPORT} -eq 0 ];then
		echo "Can't start JavaNavi Server with PORT $PORT in 30 seconds! Please check the logs!"
		return
	fi

	echo "Start JavaNavi Server with $PORT successed!";
	
}

function stop(){
	pre_kill_process $1
	exit ${RESTATUS}
}

function daemon(){
	if [ $# -lt 4 ];then
		echo "Pealse input the module name,the job name,the operation option and the job parameters."
		echo "Notice: the operation includes -start,-stop,-restart and -next.The job parameters's formation are"
		echo "key-value pairs like as 'rundata(date)=2013-01-22'."
		exit 1
	fi
	module_nm=$1
	job_nm=$2

	case "$3" in
   		-start|-stop|-restart|-next)
			opt_option=$3
			;;	
		*)
			echo "the operation option should be -start,-stop,-restart or -next!"
			exit 1
	esac

	dae_logs_dir="$LOGSDIR/$module_nm-$4"
	if [ ! -d "$dae_logs_dir" ]
	then
		su -l ${NAVIUSER} -c "mkdir $dae_logs_dir"
	fi

	JVMARGS="$JVMARGS -Dfile.log.path=$dae_logs_dir/$module_nm-$4.log"

	exec java ${JVMOPTS} ${JVMARGS} -cp ${BOOTPATH} ${DAEMONMAINCLASS} ${module_nm} ${job_nm} ${opt_option} $4 2>${dae_logs_dir}/${module_nm}-$4.err &
	
	echo "Operate Successed! You can check the result from the logs $dae_logs_dir/$module_nm-$4.log"
}

function closeport(){
	if [ ! -n "$1" ];then
		getPort_array
        for PORT in ${PORT_ARRAY[@]}
        do
            echo "Close the PORT $PORT"
            iptables -A INPUT -p TCP --dport ${PORT} -j DROP
        done
	else
		echo "Close the PORT $1"
		iptables -A INPUT -p TCP --dport $1 -j DROP
	fi
}

function openport(){
	if [ ! -n "$1" ];then
        getPort_array
        for PORT in ${PORT_ARRAY[@]}
        do
            echo "Open the PORT $PORT"
            iptables -A INPUT -p TCP --dport ${PORT} -j DROP
        done
    else
    echo "Open the PORT $1"
        iptables -A INPUT -p TCP --dport $1 -j DROP
    fi
}

function deploy(){
	if [ $# -lt 3 ];then
		echo "Usage: deploy groupId artifactId version"
		exit 1;
	fi

	groupId=$1
	artifactId=$2
	version=$3

	group=${groupId//\./\/}
	filename=${artifactId}-${version}.tar.gz
	url=http://10.105.13.86/artifactory/youku-java-private-release/${group}/${artifactId}/${version}/${filename}
	
	su -l ${NAVIUSER} -c "wget -P $MODULESDIR -N $url"
        if [ $? -ne 0 ];then
		exit 1;
        fi
	
	echo "Begin Close the Port..."
	closeport

	echo "Waiting..."
	sleep 10
	
	echo "Begein Stop JavaNavi..."
	pre_kill_process
	
	echo "Waiting..."
	sleep 10	

	if [ -d "$artifactId" ];then
		su -l ${NAVIUSER} -c "rm -rf $MODULESDIR/$artifactId"
	fi
	
	echo "Begin deploy the $artifactId..."
	su -l ${NAVIUSER} -c "tar -zxf $MODULESDIR/$filename -C $MODULESDIR"
	echo "Deploy $filename sucessed!"

	echo "Begin Start JavaNavi..."
	start
        	
	exit 0
}

function status(){
	echo -e "PORT\tPID\tLOG_FILE"
	
	for PID_FILE in `ls ${PID}SDIR`
	do	
		pid=`cat ${PID}SDIR/${PID}_FILE`
		PORT=`echo ${PID}_FILE | cut -d"." -f1`
		
		findPort ${PORT}
		if [ ${CANFINDPORT} -eq 0 ];then
			rm ${PID}SDIR/${PID}_FILE
		else
			echo -e "$PORT\t${PID}\t$LOGSDIR/$PORT/$PORT"
		fi	
	done
}

case $1 in
    	start)
		start $2
		;;
	stop)
		stop $2
		;;
	daemon)
		daemon $2 $3 $4 $5
		;;
	deploy)
		deploy $2 $3 $4 $5
		;;
	openport)
		openport $2
		;;
	closeport)
		closeport $2
		;;
	status)
		status
		;;
	*)

    echo "------------------------------------------------------------------------"
    echo $"Usage: $0 {start|stop|daemon|deploy|openport|closeport|status}"
    echo "------------------------------------------------------------------------"
    echo "where options include:"
    echo "	 start <PORT>"
    echo "		--start JavaNavi process with default PORT,you could assign a PORT."
    echo "	 stop <PORT>  "
    echo "		--stop JavaNavi process "
    echo "	 daemon [moduleName] [jobName] [operation] [jobParameter] "
    echo "		--start a JavaNavi-daemon process,Pealse input the module name,the job name,the operation option and the job parameters."
    echo "		  Notice: the operation includes -start,-stop,-restart and -next,job parameters's formation are key-value pairs like as 'rundata(date)=2013-01-22'."
    echo "	 deploy [groupId] [artifactId] [version] "
    echo "		--deploy a JavaNavi Module use the params in the artifactory."
    echo "	 openport <PORT> "
    echo "		--open PORT to accept request."
    echo "	 closeport <PORT> "
    echo "		--close PORT to refuse request."
    echo "	 status "
    echo "		--show the status of JavaNavi progress,include pid, PORT and logpath."
    echo "	 mark:parameter '[]' is Required, '<>' could not fill."
    exit 2
esac	
