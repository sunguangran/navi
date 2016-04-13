#!/bin/sh
. /etc/init.d/functions

NAVIUSER=navi

JAVA_NAVI_HOME=/opt/navi/JavaNavi
LOGSDIR=/var/log/JavaNavi
PIDSDIR=/var/lib/JavaNavi/run
#chown $NAVIUSER "$JAVA_NAVI_HOME" || exit $?

JAVA_NAVI_BIN=`dirname "$0"`
cd $JAVA_NAVI_BIN
cd ..
CURRENT_HOME=`pwd`

if [ "$JAVA_NAVI_BIN" != "/etc/init.d" ];then
	NAVIUSER=`whoami`
	#NAVIUSER=root
	JAVA_NAVI_HOME=$CURRENT_HOME
	LOGSDIR="$JAVA_NAVI_HOME/logs"
	PIDSDIR="$JAVA_NAVI_HOME/pids"
else
	chown $NAVIUSER "$JAVA_NAVI_HOME" || exit $?
	chown $NAVIUSER "$LOGSDIR" || exit $?
	chown $NAVIUSER "$PIDSDIR" || exit $?
fi

if [ ! -d "$JAVA_NAVI_HOME" ]
then
	echo "Can't find the JavaNavi home!"
	exit 1
fi

if [ ! -d "$LOGSDIR" ]
then
  mkdir -p "$LOGSDIR"
fi

if [ ! -d "$PIDSDIR" ]
then
  mkdir -p "$PIDSDIR"
fi

#echo "Java_NAVI_HOME:$JAVA_NAVI_HOME"
#echo "logdir:$LOGSDIR"
#echo "PIDSDIR:$PIDSDIR"
#echo "NAVIUSER:$NAVIUSER"

#LOGSDIR=/var/log/JavaNavi
#chown $NAVIUSER "$LOGSDIR" || exit $?

#PIDSDIR=/var/lib/JavaNavi/run
#chown $NAVIUSER "$PIDSDIR" || exit $?

MODULESDIR="$JAVA_NAVI_HOME/NaviModules"

JVMOPTS="-server -Xmx1024m -Xms1024m -XX:NewSize=384m -Xss512k -XX:+UseConcMarkSweepGC -XX:+UseParNewGC"
JVMARGS="-DJAVA_NAVI_HOME=$JAVA_NAVI_HOME -Dfile.encoding=UTF-8 -Dlog4j.configuration=log4j.configuration"
NAVIMAINCLASS="com.youku.java.navi.boot.NaviMain"
DAEMONMAINCLASS="com.youku.java.navi.boot.NaviDaemonMain"
BOOTPATH="$JAVA_NAVI_HOME/NaviLibs/JavaNaviBoot-0.1.4-release.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-core-2.2.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-api-2.2.jar:$JAVA_NAVI_HOME/NaviLibs/log4j-1.2-api-2.2.jar"
CONFIGPATH="$JAVA_NAVI_HOME/conf/server.conf"


function pre_kill_process()
{
	cd $PIDSDIR
	restatus=0
	if [ ! $# -eq 0 ];then
		echo "The JavaNavi Server with port $1 is stopping..."
		pid_file=$1.pid
		if [ ! -f "$pid_file" ];then
			echo "Can't find the pid file, stop JavaNavi Server failed!"
			restatus=1
			return
		fi
		kill_process `cat $1.pid`
		if [ $restatus -eq 0 ];then
			rm $pid_file
		fi
		return
	fi

	for pid_file in `ls .`
	do
		pid=`cat $pid_file`
		port=`echo $pid_file | cut -d"." -f1`
		echo "The JavaNavi Server with port $port is stopping..."
		kill_process $pid 
		if [ $restatus -eq 1 ];then
			continue
		fi
		rm $pid_file
	done

}

function kill_process(){
	pid=$1
	if checkpid $pid;then
		kill $pid >/dev/null 2>&1
		if [ $? -eq 0 ];then
			echo "Killed process $pid, stop JavaNavi Server successed!"
			restatus=0
		else
			restatus=1
			echo "Can't kill process $pid, stop JavaNavi Server failed!"
		fi
	else
		echo "Process $pid isn't exist, stop JavaNavi Server failed!"
	fi
}

function getPort_array(){
	ports=`egrep ^listen.port= $CONFIGPATH | cut -d"=" -f2`
    OLD_IFS=$IFS
    IFS=","
    port_array=($ports)
    IFS=$OLD_IFS
}

function findPort(){
	CANFINDPORT=`netstat -vatn|grep LISTEN|grep $1|wc -l`
}

function start(){
	if [ $# -eq 0 ];then
		getPort_array
		for port in ${port_array[@]}
		do
			start_navi $port
		done
	else
		start_navi $1
	fi	
}


function start_navi(){
	
	port=$1
	findPort $port

	if [ $CANFINDPORT -ne 0 ];then
		echo "The port $port has already used! Start JavaNavi Server with port $port failed!"
		return
	fi

	default_log_dir="$LOGSDIR/$port"

	if [ ! -d "$default_log_dir" ]
	then
		su -l $NAVIUSER -c "mkdir $default_log_dir"
	fi

	file_log_path="$default_log_dir/$port"
	error_log_path="$default_log_dir/$port.err"
	pid_path="$PIDSDIR/$port.pid"
	NEWJVMARGS="$JVMARGS -Dfile.log.path=$file_log_path -Dlisten.port=$port"

	NAVI_TEMP_FILE=$JAVA_NAVI_HOME/bin/navi_temp.sh

	echo "JavaNavi Server with port $port is starting..."
	
	echo "exec java $JVMOPTS $NEWJVMARGS -cp $BOOTPATH $NAVIMAINCLASS  2>$error_log_path &" > $NAVI_TEMP_FILE
	echo "echo \$! > $pid_path" >> $NAVI_TEMP_FILE
	echo "exit \$?" >> $NAVI_TEMP_FILE
	
	su -l $NAVIUSER -c "sh $NAVI_TEMP_FILE"
	
	if [ $? -ne 0 ];then
		echo "Start JavaNavi Server with port $port failed! Please check the logs!"
		rm $pid_path
		rm $NAVI_TEMP_FILE
		return
	fi

	rm $NAVI_TEMP_FILE
	findPort $port
	nbSeconds=1

	while [ $CANFINDPORT -eq 0 ] && [ $nbSeconds -lt 5 ];do
		sleep 1
		let "nbSeconds = $nbSeconds + 1"
		findPort $port
	done
	
	if [ $CANFINDPORT -eq 0 ];then
		echo "Can't start JavaNavi Server with port $port in 30 seconds! Please check the logs!"
		return
	fi

	echo "Start JavaNavi Server with $port successed!";
	
}

function stop(){
	pre_kill_process $1
	exit $restatus
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
		su -l $NAVIUSER -c "mkdir $dae_logs_dir"
	fi
	JVMARGS="$JVMARGS -Dfile.log.path=$dae_logs_dir/$module_nm-$4.log"
	

	exec java $JVMOPTS $JVMARGS -cp $BOOTPATH $DAEMONMAINCLASS $module_nm $job_nm $opt_option $4 2>$dae_logs_dir/$module_nm-$4.err &
	
	echo "Operate Successed! You can check the result from the logs $dae_logs_dir/$module_nm-$4.log"
}

function closeport(){
	if [ ! -n "$1" ];then
		getPort_array
        	for port in ${port_array[@]}
        	do
                	echo "Close the port $port"
                	iptables -A INPUT -p TCP --dport $port -j DROP
        	done
	else
		echo "Close the port $1"
		iptables -A INPUT -p TCP --dport $1 -j DROP
	fi
}

function openport(){
	if [ ! -n "$1" ];then
                getPort_array
                for port in ${port_array[@]}
                do
                        echo "Open the port $port"
                        iptables -A INPUT -p TCP --dport $port -j DROP
                done
        else
		echo "Open the port $1"
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
	filename=$artifactId-$version.tar.gz
	url=http://10.105.13.86/artifactory/youku-java-private-release/$group/$artifactId/$version/$filename
	
	su -l $NAVIUSER -c "wget -P $MODULESDIR -N $url"

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
		su -l $NAVIUSER -c "rm -rf $MODULESDIR/$artifactId"
	fi
	
	echo "Begin deploy the $artifactId..."
	su -l $NAVIUSER -c "tar -zxf $MODULESDIR/$filename -C $MODULESDIR"
	echo "Deploy $filename sucessed!"

	echo "Begin Start JavaNavi..."
	start
        	
	exit 0
}

function status(){
	echo -e "PORT\tPID\tLOG_FILE"
	
	for pid_file in `ls $PIDSDIR`
	do	
		pid=`cat $PIDSDIR/$pid_file`
		port=`echo $pid_file | cut -d"." -f1`
		
		findPort $port
		if [ $CANFINDPORT -eq 0 ];then 
			rm $PIDSDIR/$pid_file
		else
			echo -e "$port\t$pid\t$LOGSDIR/$port/$port"
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
		echo "	 start <port>" 
		echo "		--start JavaNavi process with default port,you could assign a port."
		echo "	 stop <port>  "
		echo "		--stop JavaNavi process "
		echo "	 daemon [moduleName] [jobName] [operation] [jobParameter] "
		echo "		--start a JavaNavi-daemon process,Pealse input the module name,the job name,the operation option and the job parameters."
		echo "		  Notice: the operation includes -start,-stop,-restart and -next,job parameters's formation are key-value pairs like as 'rundata(date)=2013-01-22'."
		echo "	 deploy [groupId] [artifactId] [version] "
		echo "		--deploy a JavaNavi Module use the params in the artifactory."
		echo "	 openport <port> "
		echo "		--open port to accept request."
		echo "	 closeport <port> "
		echo "		--close port to refuse request."
		echo "	 status "
		echo "		--show the status of JavaNavi progress,include pid, port and logpath."
		echo "	 mark:parameter '[]' is Required, '<>' could not fill."
		exit 2
esac	
