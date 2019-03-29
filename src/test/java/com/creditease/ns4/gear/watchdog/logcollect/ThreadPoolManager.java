package com.creditease.ns4.gear.watchdog.logcollect;

import java.util.concurrent.*;

public class ThreadPoolManager {
    private static ThreadPoolManager instance = new ThreadPoolManager();

    private ExecutorService secLogThreadPool;
    private ExecutorService sysLogThreadPool;

    public ExecutorService getSysLogThreadPool() {
        return sysLogThreadPool;
    }

    public void setSysLogThreadPool(ExecutorService sysLogThreadPool) {
        this.sysLogThreadPool = sysLogThreadPool;
    }

    public ExecutorService getSecLogThreadPool() {
        return secLogThreadPool;
    }

    public void setSecLogThreadPool(ExecutorService secLogThreadPool) {
        this.secLogThreadPool = secLogThreadPool;
    }

    public static ThreadPoolManager getInstance(){
        return instance;
    }

    private ThreadPoolManager() {
        secLogThreadPool = new ThreadPoolExecutor(1, 3, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000), new ThreadPoolExecutor.CallerRunsPolicy());
        sysLogThreadPool = Executors.newFixedThreadPool(3);
    }
}
