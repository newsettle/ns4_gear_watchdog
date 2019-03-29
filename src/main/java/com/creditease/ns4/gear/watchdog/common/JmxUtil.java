package com.creditease.ns4.gear.watchdog.common;

import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataMXBean;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * @author outman
 * @description Jmx辅助类
 * @date 2019/1/15
 */
public class JmxUtil {

    public static final String DOMAIN = "com.creditease";

    public static MBeanServer getMBeanServer() {
        MBeanServer server;
        //if (!MBeanServerFactory.findMBeanServer(null).isEmpty()) {
        //    server = MBeanServerFactory.findMBeanServer(null).get(0);
        //} else {
        server = ManagementFactory.getPlatformMBeanServer();
        //}
        return server;
    }

    public static MonitorDataMXBean getMonitorDataMXBean(MBeanServerConnection mbsc) throws MalformedObjectNameException {
        ObjectName mbeanName = new ObjectName(MonitorDataMXBean.MBEAN_OBJECT_NAME);
        return MBeanServerInvocationHandler.newProxyInstance(mbsc, mbeanName, MonitorDataMXBean.class, false);
    }
}
