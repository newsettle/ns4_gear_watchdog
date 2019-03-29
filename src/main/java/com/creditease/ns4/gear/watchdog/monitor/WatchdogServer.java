package com.creditease.ns4.gear.watchdog.monitor;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;
import com.creditease.ns4.gear.watchdog.monitor.jmx.server.business.NSBusiness;
import com.creditease.ns4.gear.watchdog.monitor.process.WatchdogChildTask;
import com.creditease.ns4.gear.watchdog.monitor.process.shutdown.ShutdownHandler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author outman
 * @description Watchdog服务入口
 * @date 2019/1/15
 */
public class WatchdogServer {
    private static final NsLog logger = NsLogger.getWatchdogLogger();

    private Long startTime = System.currentTimeMillis();

    private static WatchdogServer server = new WatchdogServer();

    public static WatchdogServer instance() {
        return server;
    }

    public void start() {
        // 注册停止进程处理信号
        new ShutdownHandler().registerSignal();
        // 启动子进程服务
        WatchdogChildTask.getInstance().init();
        // 启动JMX服务
        Jmx.getInstance().start();

        // 输出启动info
        logger.info("============== WatchdogServer started ==============");

        //启动数据收集定时器
        saveESDataTimer();
    }

    public static void main(String[] args) {
        WatchdogServer.instance().start();
    }

    public WatchdogChildTask getWatchdogChildTask() {
        return WatchdogChildTask.getInstance();
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void saveESDataTimer(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                logger.info(System.currentTimeMillis() +"----设定要指定任务-----");
                NSBusiness.saveTradeDataToEs();
            }
        }, 2*60*1000, 60000);
    }

}











