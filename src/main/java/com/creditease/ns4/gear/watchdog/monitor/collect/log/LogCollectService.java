package com.creditease.ns4.gear.watchdog.monitor.collect.log;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadPoolManager;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.monitor.collect.CollectService;
import org.apache.flume.node.Application;

import java.util.concurrent.ExecutorService;

/**
 * @author outman
 * @description 日志归集service
 * @date 2019/2/28
 */
public class LogCollectService implements CollectService {

    private static final NsLog logger = NsLogger.getWatchdogLogger();

    /**
     * 采集器所依赖的配置文件的路径
     */
    public static final String COLLECT_CONF = "config/logcollect.conf";
    /**
     * 采集器的名称
     */
    public static final String COLLECT_NAME = "collect";

    /**
     * 启动日志采集器所需的参数
     */
    private String[] argms;

    private ExecutorService logCollectPool;

    @Override
    public void prepare() {
        //-n agent -f conf/flume-es-file.conf -Dflume.root.logger=DEBUG,console
        String confPath = getConfPath();
        this.argms = new String[]{"-n", COLLECT_NAME, "-f", confPath};
        logCollectPool = ThreadPoolManager.getInstance().newFixedThreadPool("logcollect-%d", 1);
    }

    @Override
    public void boot() {
        try {
            logCollectPool.execute(new Runnable() {
                @Override
                public void run() {
                    Application.main(argms);
                }
            });
        } catch (Exception e) {
            logger.error("创建日志归集线程池异常：{}", e.getMessage());
        }
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void shutdown() {
        if (logCollectPool != null) {
            this.logCollectPool.shutdown();
            this.logCollectPool = null;
        }
    }

    /**
     * 获取配置的路径
     *
     * @return conf文件所在的路径
     */
    private String getConfPath() {
        return this.getClass().getClassLoader().getResource(COLLECT_CONF).getPath();
    }
}
