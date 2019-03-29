package com.creditease.ns4.gear.watchdog.monitor.process;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.constant.ServerVendor;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadPoolManager;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.template.TemplateName;
import com.creditease.ns4.gear.watchdog.monitor.collect.jvm.JVMCollectService;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.LogCollectService;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;
import com.creditease.ns4.gear.watchdog.monitor.notify.NotifyRoute;
import com.creditease.ns4.gear.watchdog.monitor.notify.constant.MESSAGE;
import com.creditease.ns4.gear.watchdog.monitor.process.config.NS4AppConf;
import com.creditease.ns4.gear.watchdog.monitor.process.constant.PROCES;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author outman
 * @description 子进程控制任务类
 * @date 2019/1/15
 */
public class WatchdogChildTask implements Runnable {

    private static final NsLog logger = NsLogger.getWatchdogLogger();

    private static WatchdogChildTask watchdogChildTask = null;

    /**
     * 是否重启
     */
    private boolean isRestart = true;
    /**
     * 操作 启动(start)、停止(stop)、重启(restart)<br>
     * 默认值 停止
     */
    private PROCES.Options options = PROCES.Options.STOP;
    /**
     * 进程信息
     */
    private ProcessInfo processInfo;
    /**
     * 子进程
     */
    private WatchdogChildProcess process;
    /**
     * 服务启动入口
     */
    private ServerVendor serverVendor = null;
    /**
     * ns4 app 配置信息
     */
    private NS4AppConf ns4AppConf = null;
    /**
     * 日志归集
     */
    private LogCollectService logCollectService = null;
    /**
     * 系统指标采集
     */
    private JVMCollectService jvmCollectService = null;

    /**
     * 停止子进程线程池
     */
    private ExecutorService stopChildProcessPool = null;
    /**
     * 启动子进程线程池
     */
    private ExecutorService startChildProcessPool = null;


    /**
     * 获取 watchdog 子进程控制任务单例
     */
    public static WatchdogChildTask getInstance() {
        if (watchdogChildTask == null) {
            watchdogChildTask = new WatchdogChildTask();
        }
        return watchdogChildTask;
    }

    /**
     * 启动 watchdog 子进程
     */
    public void start() {
        if (this.options == PROCES.Options.START) {
            logger.info("子进程已经启动");
            return;
        }
        logger.info("开始启动子进程服务");
        this.options = PROCES.Options.START;
        //启动日志归集
        //this.logCollectService.boot();
        this.startChildProcessPool.execute(this);
    }

    /**
     * 启动 watchdog 子进程
     */
    public void init() {
        if (this.options == PROCES.Options.START) {
            logger.info("子进程已经启动");
            return;
        }
        //创建进程信息
        if (this.processInfo == null) {
            this.processInfo = new ProcessInfo();
        }
        //创建ns4 app 配置
        if (this.ns4AppConf == null) {
            this.ns4AppConf = new NS4AppConf();
            //设置到进程信息里
            this.processInfo.setNs4AppConf(this.ns4AppConf);
        }
        String serverMain = this.ns4AppConf.getServerMain();
        if (ServerVendor.NSDISPATCHER.toString().equals(serverMain)) {
            this.serverVendor = ServerVendor.NSDISPATCHER;
        } else if (ServerVendor.NSTRANSPORTER.toString().equals(serverMain)) {
            this.serverVendor = ServerVendor.NSTRANSPORTER;
        } else { //默认按照NSTransporter进行支撑
            this.serverVendor = ServerVendor.NSTRANSPORTER;
        }
        try {
            this.startChildProcessPool = ThreadPoolManager.getInstance().newExecutorService("start-child-process-%d", 1, 1, 0L, TimeUnit.MILLISECONDS, 1);
        } catch (Exception e) {
            logger.error("创建启动子进程线程池异常：{}", e.getMessage());
        }
        try {
            this.stopChildProcessPool = ThreadPoolManager.getInstance().newExecutorService("stop-child-process-%d", 1, 1, 0L, TimeUnit.MILLISECONDS, 1);
        } catch (Exception e) {
            logger.error("创建停止子进程线程池异常：{}", e.getMessage());
        }
        //初试化启动日志归集器(可以统一调整为CollectService启动，首先判断是否启动，然后做动态启动)
        if (Boolean.TRUE.equals(PropertiesUtil.getBoolean("ns4.app.log.enabled"))) {
            this.logCollectService = new LogCollectService();
            this.logCollectService.prepare();
            this.logCollectService.boot();
        }
        //jvm 进程 采集
        this.jvmCollectService = new JVMCollectService();
        this.jvmCollectService.boot();
        this.start();
    }

    /**
     * 停止 watchdog 子进程
     */
    public void stop() {
        logger.info("开始停止子进程服务");
        if (this.options == PROCES.Options.STOP) {
            logger.info("子进程已经停止运行");
            return;
        }
        if (this.process != null) {
            this.options = PROCES.Options.STOP;
            this.process.stop();
            //通知JMX - 已经停止pid
            Jmx.getInstance().stopChildJmx(this.processInfo.getPid());
            logger.info("停止子进程({})服务完成", this.processInfo.getPid());
        }
    }

    /**
     * 重启 watchdog 子进程
     */
    public void restart() {
        if (!this.isRestart) {
            logger.info("重启子进程正在进程中");
            return;
        }
        this.isRestart = false;
        logger.info("开始重启子进程服务");
        if (this.options != PROCES.Options.STOP) {
            logger.info("重启-步骤 1 停止子进程服务");
            this.stopChildProcessPool.execute(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            });
            try {
                logger.info("正在处理停止子进程...");
                synchronized (this) {
                    this.wait();
                }
            } catch (Exception e) {
                logger.error("重启子进程异常:{}", e.getMessage());
            }
        } else {
            logger.info("子进程已经停止，直接启动子进程");
        }
        logger.info("重启-步骤 2 处理停止子进程结束，准备启动子进程");
        this.start();
        this.isRestart = true;
    }


    @Override
    public void run() {
        logger.info("准备启动子进程");
        try {
            int i = 0;
            long retry = Long.MAX_VALUE;
            //重试启动
            while (i++ < retry) {
                try {
                    //判断操作
                    if (this.options == PROCES.Options.STOP) {
                        //操作-停止 -> 跳出重试启动while，并尝试kill子进程
                        logger.info("重试启动子进程退出");
                        break;
                    } else if (this.options == PROCES.Options.START || this.options == PROCES.Options.RESTART) {
                        //操作-启动 获取并设置app启动入口类
                        this.processInfo.setAppMain(PROCES.APP_MAIN.get(this.serverVendor.toString()));
                        WatchdogChildProcess process = new WatchdogChildProcess(this.processInfo);
                        this.process = process;
                        process.create();
                    }
                } catch (Exception e) {
                    logger.error("重试启动子进程异常：{} {}", e.getMessage(), e);
                    NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.ERROR, this.processInfo, e.getMessage());
                } finally {
                    if (this.process != null) {
                        NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.STOP, this.processInfo, null);
                        logger.warn("子进程退出：{}", this.process.getProcessInfo().getExitMessage());
                        this.process.kill();
                        this.process.clear();
                        this.process = null;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("重试启动子任务异常-RUN：{} {}", e.getMessage(), e);
            NotifyRoute.send(TemplateName.TEMP_NAME_CHAT_PROCESS_STATUS, MESSAGE.TYPE.ERROR, this.processInfo, e.getMessage());
        } finally {
            if (this.options == PROCES.Options.STOP) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
        }
    }

    /**
     * 获取进程信息
     */
    public ProcessInfo getProcessInfo() {
        WatchdogChildProcess process = this.process;
        if (process != null) {
            return process.getProcessInfo();
        }
        return this.processInfo;
    }

    /**
     * 清除 释放资源
     */
    void clear(PROCES.Options options) {
        if (options != null) {
            this.options = options;
        }
        logger.info("清除 Watchdog Child Task 资源开始");
        this.stop();
        if (this.process != null) {
            this.process.clear();
            this.process = null;
        }
        if (this.stopChildProcessPool != null) {
            this.stopChildProcessPool.shutdown();
            this.stopChildProcessPool = null;
        }
        if (this.startChildProcessPool != null) {
            this.startChildProcessPool.shutdown();
            this.startChildProcessPool = null;
        }
        this.processInfo = null;
        logger.info("清除 Watchdog Child Task 资源结束");
    }

    /**
     * 销毁并释放资源
     */
    public void destroy() {
        this.clear(PROCES.Options.STOP);
        try {
            logger.info("停止日志归集线程池");
            this.logCollectService.shutdown();
        } catch (Throwable throwable) {
            logger.error("停止日志归集线程异常：{}", throwable.getMessage());
        }
    }
}
