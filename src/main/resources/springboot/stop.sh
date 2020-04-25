#!/bin/bash

script=`readlink -f $0`
script_path=${script%/*}

pidfile=${script_path}/.pidfile

if [ ! -f "$pidfile" ];then
    echo 'error: pidfile not exists!'
    exit
fi

pid=`cat $pidfile`
echo "stop process with pid $pid"
kill $pid
rm ${pidfile}