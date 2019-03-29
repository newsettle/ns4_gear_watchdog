#!/bin/bash  -vx
#author

JVM_OPTS="-Dfile.encoding=utf-8  -Duser.timezone=GMT+8 -server -Xms1024m -Xmx1024m  -XX:MaxPermSize=128m  -XX:+UseConcMarkSweepGC -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=5 -XX:+PrintGC -XX:+PrintGCTimeStamps  -XX:+PrintGCDetails  -XX:+PrintGCApplicationStoppedTime -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDateStamps -Xloggc:watchdog/log/gc.log"

echo Starting .........

cd ..
CLASS_PATH=""
export LANG="en_US.UTF-8"

for i in $PWD/watchdog/lib/monitor/*;
    do CLASS_PATH=$i:"$CLASS_PATH";
done

CLASS_PATH=.:$PWD/watchdog/:$CLASS_PATH

nohup java $JVM_OPTS -classpath $CLASS_PATH com.creditease.ns4.gear.watchdog.monitor.WatchdogServer >$PWD/log/stdout.log 2>&1 &