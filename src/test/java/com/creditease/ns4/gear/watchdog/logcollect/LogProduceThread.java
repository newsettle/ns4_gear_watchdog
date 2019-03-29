package com.creditease.ns4.gear.watchdog.logcollect;

public class LogProduceThread implements Runnable {
    LogEntity entity = null;

    public LogProduceThread(LogEntity entity) {
        this.entity = entity;
    }

    @Override
    public void run() {
        LogStorage.getInstance().produce(entity);
    }
}

