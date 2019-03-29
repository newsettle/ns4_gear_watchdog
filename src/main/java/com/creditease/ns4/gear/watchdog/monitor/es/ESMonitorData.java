package com.creditease.ns4.gear.watchdog.monitor.es;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.JmxUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.monitor.MessageType;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataFrame;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataMXBean;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;

import javax.management.MBeanServerConnection;

public class ESMonitorData {
    private static final NsLog logger = NsLogger.getWatchdogLogger();

    //从jmx中获取数据
    public String getDataFromJMX() {
        String jmxData = "";
        try {
            MBeanServerConnection mbsc = Jmx.getInstance().getChildJmx().getMbServerConnection();
            MonitorDataMXBean proxy = JmxUtil.getMonitorDataMXBean(mbsc);
            jmxData =  proxy.getData(MessageType.NS_TRADE_MESSAGE.name());
        } catch (Exception e) {
            logger.error("获取接收队列异常：{} {}", e.getMessage(), e);
        }
       return jmxData;
    }
}
