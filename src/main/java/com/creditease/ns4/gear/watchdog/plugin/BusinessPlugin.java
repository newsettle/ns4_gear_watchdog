package com.creditease.ns4.gear.watchdog.plugin;

import com.creditease.framework.ext.plugin.MonitorEvent;
import com.creditease.framework.ext.plugin.MonitorListener;
import com.creditease.framework.ext.plugin.MonitorPlugin;
import com.creditease.framework.ext.plugin.NSEnvironment;
import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.plugin.listeners.MonitorTradeListeners;

import java.util.ArrayList;
import java.util.List;

/**
 * @author outman
 * @description NS业务监控插件实现
 * @date 2019/1/25
 */
public class BusinessPlugin extends AbstractPlugin implements MonitorPlugin {
    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();

    private List<MonitorListener> monitorListeners;

    @Override
    public List<MonitorListener> getMonitorListeners() {
        return monitorListeners;
    }

    @Override
    public void load(NSEnvironment nsEnvironment) {
        logger.info("BusinessPlugin.load");
        monitorListeners = new ArrayList<>();
        MonitorTradeListeners tradeListeners = new MonitorTradeListeners();
        monitorListeners.add(tradeListeners );

        this.loaded = true;
    }

    @Override
    public void unload() {
        logger.info("卸载完毕");
    }
}
