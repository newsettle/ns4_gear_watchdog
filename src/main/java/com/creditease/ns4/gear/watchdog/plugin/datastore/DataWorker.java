package com.creditease.ns4.gear.watchdog.plugin.datastore;

import com.creditease.framework.ext.plugin.NSEnvironment;
import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.JmxUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataMXBean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author outman
 * @description 动态注册、卸载业务监控MBean
 * @date 2019/1/15
 */
public class DataWorker {

    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();

    private MBeanServer server;

    private Map<ObjectName, MonitorDataMXBean> profileMBeanMap = new HashMap<ObjectName, MonitorDataMXBean>();

    private static DataWorker instance = null;

    public static DataWorker instance() {
        if (instance == null) {
            instance = new DataWorker();
        }
        return instance;
    }

    public void start() {
        if (server == null) {
            server = JmxUtil.getMBeanServer();
        }
        this.installMBean(); //此处可以动态添加业务监控指标
    }

    public void start(NSEnvironment nsEnvironment) {
        if (server == null) {
            server = JmxUtil.getMBeanServer();
        }
        //动态添加业务监控Mbean
        this.installMBean(nsEnvironment);
    }

    public void stop() {
        for (Entry<ObjectName, MonitorDataMXBean> entry : profileMBeanMap.entrySet()) {
            try {
                server.unregisterMBean(entry.getKey());
            } catch (Exception e) {
                // ignore
            }
        }
        profileMBeanMap.clear();
    }

    public void installMBean() {
        try {
            ObjectName mbeanName = new ObjectName(MonitorDataMXBean.MBEAN_OBJECT_NAME);
            if (server.isRegistered(mbeanName)) {
                this.uninstallMBean();
            }
            MonitorData monitorMBean = new MonitorData();
            server.registerMBean(monitorMBean, mbeanName);
            profileMBeanMap.put(mbeanName, monitorMBean);
            logger.info("注册{} MBean完成", mbeanName);
        } catch (Exception e) {
            logger.error("installMBean : {} {}", e.getMessage(), e);
        }
    }

    public void installMBean(NSEnvironment nsEnvironment) {
        try {
            ObjectName mbeanName = new ObjectName(MonitorDataMXBean.MBEAN_OBJECT_NAME);
            if (server.isRegistered(mbeanName)) {
                this.uninstallMBean();
            }
            MonitorData monitorMBean = new MonitorData(nsEnvironment);
            server.registerMBean(monitorMBean, mbeanName);
            profileMBeanMap.put(mbeanName, monitorMBean);
            logger.info("注册{} MBean完成", mbeanName);
        } catch (Exception e) {
            logger.error("installMBean : {} {}", e.getMessage(), e);
        }
    }

    public void uninstallMBean() {
        ObjectName mbeanName;
        try {
            mbeanName = new ObjectName(MonitorDataMXBean.MBEAN_OBJECT_NAME);
            server.unregisterMBean(mbeanName);
            profileMBeanMap.remove(mbeanName);
        } catch (Exception e) {
            logger.error("uninstallMBean : {} {}", e.getMessage(), e);
        }
    }
}
