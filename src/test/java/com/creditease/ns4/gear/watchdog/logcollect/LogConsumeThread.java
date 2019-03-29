package com.creditease.ns4.gear.watchdog.logcollect;

public class LogConsumeThread implements Runnable {
    @Override
    public void run() {
        while(true){
            //TODO do something here
            LogStorage.getInstance().consume();
        }
    }
}
