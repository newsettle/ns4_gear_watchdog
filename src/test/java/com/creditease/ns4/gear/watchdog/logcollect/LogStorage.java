package com.creditease.ns4.gear.watchdog.logcollect;

import java.util.concurrent.LinkedBlockingDeque;

public class LogStorage {
    private final int MAX_SIZE = 100;
    private LinkedBlockingDeque<LogEntity> list = new LinkedBlockingDeque<LogEntity>(MAX_SIZE);
    private static LogStorage instance = new LogStorage();

    private LogStorage() {
    }

    public static LogStorage getInstance() {
        return instance;
    }

    public void produce(LogEntity seclog) {
        if (list.size() == MAX_SIZE) {
            System.out.println("seclog库存量为" + MAX_SIZE + ",不能再继续生产！");
        }
        try {
            list.put(seclog);
            System.out.println("生产SecLog："+ seclog);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LogEntity consume(){
        LogEntity entity = null;
        if(list.isEmpty()){
            System.out.println("seclog库存量为0，不能再继续消费！");
        }
        try {
            entity = list.take();
            System.out.println("消费SecLog："+entity);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return entity;
    }
}

