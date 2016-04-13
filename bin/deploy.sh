#!/bin/bash

if [ ! -n "$1" ] || [ ! -n "$2" ] || [ ! -n "$3" ];then
	echo "error parameter!"
	exit;
fi

groupId=$1
artifactId=$2
version=$3

if [ ! -n "$4" ];then
	mode=release
else
	mode=test
fi

group=${groupId//\./\/}

filename=$artifactId-$version.tar.gz

path=http://10.105.13.86/artifactory/youku-java-private-$mode/$group/$artifactId/$version/$filename

echo "Deploy begin..."

if [ -d "$artifactId" ];then
        rm -rf $artifactId
fi

wget -N $path

if [ $? -ne 0 ];then
	exit 1;
fi

tar -zxf $filename

rm ../NaviModules/$filename
mv $filename ../NaviModules/

if [ -d "../NaviModules/$artifactId" ];then
	rm -rf ../NaviModules/$artifactId
fi
mv $artifactId ../NaviModules/

echo "Deploy done!"

exit 0;
