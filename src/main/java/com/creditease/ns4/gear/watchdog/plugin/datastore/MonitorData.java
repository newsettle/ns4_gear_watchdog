package com.creditease.ns4.gear.watchdog.plugin.datastore;

import com.creditease.framework.ext.plugin.NSEnvironment;
import com.creditease.ns4.gear.watchdog.common.jvm.report.JVMMonitor;
import com.creditease.ns4.gear.watchdog.common.monitor.MessageType;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataFrame;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataMXBean;

/**
 * @author outman
 * @description 定义MonitorDataMXBean的实现
 * @date 2019/1/15
 */
public class MonitorData implements MonitorDataMXBean {

    private NSEnvironment nsEnvironment;

    public MonitorData() {
    }

    public MonitorData(NSEnvironment nsEnvironment) {
        this.nsEnvironment = nsEnvironment;
    }

    @Override
    public String getData(String messageType) {
        Object msg = MonitorDataFrame.getInstance().getData(MessageType.valueOf(messageType), this.nsEnvironment);
        if (msg != null) {
            return msg.toString();
        }
        return "";
    }

    @Override
    public String getMonitorReport() {
        return JVMMonitor.getData();
    }
}
