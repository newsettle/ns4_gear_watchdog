package com.creditease.ns4.gear.watchdog.logcollect;

public class Main {
    public static void main(String[] args) {
        LogEntity log1 = new LogEntity();
        log1.setLogName("test1");
        log1.setLogSrc("seclog1");

        LogEntity log2 = new LogEntity();
        log2.setLogName("test2");
        log2.setLogSrc("seclog2");

        ThreadPoolManager.getInstance().getSecLogThreadPool().execute(new LogProduceThread(log1));
        ThreadPoolManager.getInstance().getSecLogThreadPool().execute(new LogProduceThread(log2));
        ThreadPoolManager.getInstance().getSecLogThreadPool().execute(new LogConsumeThread());
    }
}
