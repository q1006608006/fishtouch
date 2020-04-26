#!/bin/bash

app_jar=@script.mainJar@

script=`readlink -f $0`
script_path=${script%/*}
home_path=${script_path%/*}

pidfile=${script_path}/.pidfile
if [ -e $pidfile ];then
  pid=`cat $pidfile`
  if [ `ps -p $pid|wc -l` -gt 1 ];then
    echo "process($pid) is in running,please stop it or remove the file '.pidfile'"
    exit
  fi
fi

param=$*

#you can add your jvm-properties in here
PROPERTIES=""

prop_path=${home_path}/conf
lib_path=${home_path}/libs

#if you has your jar-libs,you can add them in here
extra_path=

loader_path=${prop_path},${lib_path}

if [ -n "$extra_path" ]; then
  loader_path=${loader_path},${extra_path}
fi

BASE_CMD="java ${PROPERTIES} -Dloader.path=${loader_path} -jar ${lib_path}/${app_jar} "

CMD="${BASE_CMD} ${param}"

echo "${CMD}"
nohup ${CMD} > /dev/null 2>&1 &

echo $!
echo $! > ${script_path}/.pidfile