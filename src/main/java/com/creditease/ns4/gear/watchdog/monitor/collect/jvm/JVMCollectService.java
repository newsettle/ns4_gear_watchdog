package com.creditease.ns4.gear.watchdog.monitor.collect.jvm;

import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadFactoryBuilder;
import com.creditease.ns4.gear.watchdog.monitor.collect.CollectService;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author outman
 * @description 实时归集系统指标(process 、 memory 、 thread)
 * @date 2019/2/27
 */
public class JVMCollectService implements CollectService {

    //public static int BUFFER_SIZE = 60 * 10;

    private LinkedBlockingQueue<Map<String, Object>> queue;
    private volatile ScheduledFuture<?> collectMetricFuture;
    private volatile ScheduledFuture<?> sendMetricFuture;
    private int loopTime = PropertiesUtil.getInteger("chatbot.loop.time");
    private Sender sender;

    @Override
    public void prepare() {
        //queue = new LinkedBlockingQueue<Map<String, Object>>(BUFFER_SIZE);
        sender = new Sender();
    }

    @Override
    public void boot() {
        this.prepare();
//        collectMetricFuture = Executors
//                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("JVMService-produce").build())
//                .scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
        int taskTime = this.loopTime * 60;//单位分
        sendMetricFuture = Executors
                .newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("JVMService-consume").build())
                .scheduleAtFixedRate(sender, 0, taskTime, TimeUnit.SECONDS);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void shutdown() {
        collectMetricFuture.cancel(true);
        sendMetricFuture.cancel(true);
    }


    private class Sender implements Runnable {
        @Override
        public void run() {
            ProcessMonitor.newProcessMonitor().collection();
        }
    }
}
