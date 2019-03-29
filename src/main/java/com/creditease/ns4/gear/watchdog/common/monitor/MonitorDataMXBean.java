package com.creditease.ns4.gear.watchdog.common.monitor;

import com.creditease.ns4.gear.watchdog.common.JmxUtil;

/**
 * @author outman
 * @description 监控数据MBean定义
 * @date 2019/1/15
 */
public interface MonitorDataMXBean {

    /**
     * 监控Mbean的名称
     */
    String MBEAN_OBJECT_NAME = JmxUtil.DOMAIN + ":name=MonitorService";

    /**
     * 获取监控数据
     *
     * @param messageType 数据类型
     * @return 监控数据
     */
    String getData(String messageType);

    /**
     * 获取进程监控数据
     *
     * @return 进程监控
     */
    String getMonitorReport();
}
