package com.creditease.ns4.gear.watchdog.plugin;

import com.creditease.framework.ext.plugin.NSEnvironment;
import com.creditease.ns.log.NsLog;
import com.creditease.ns.transporter.context.XmlAppTransporterContext;
import com.creditease.ns4.gear.watchdog.common.PingUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.plugin.datastore.DataWorker;

/**
 * @author outman
 * @description NS服务与父进程通讯以及注册Mbean服务的插件
 * @date 2019/1/25
 */
public class ServicePlugin extends AbstractPlugin {
    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();

    private NSEnvironment nsEnvironment;


    public ServicePlugin() {
        this.name = "Watchdog-Service-Plugin";
    }

    @Override
    public void load(NSEnvironment nsEnvironment) {
        this.nsEnvironment = nsEnvironment;
        //启动与父进程进行通讯
        String cmdArgs = this.properties.getProperty(XmlAppTransporterContext.ALL_CMD_LINE_ARGS);
        logger.info("args ==> {}", cmdArgs);
        PingUtil.ping(cmdArgs.split(" "));
        //注册业务Mbean
        DataWorker.instance().start(this.nsEnvironment);
        //设置加载成功
        this.loaded = true;
    }

    @Override
    public void unload() {
        //取消Mbean
        DataWorker.instance().stop();
    }
}
