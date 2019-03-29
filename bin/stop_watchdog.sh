#!/bin/bash -vx
#stop watchdog
kill -15 `jps |grep WatchdogServer|awk '{print $1}'`