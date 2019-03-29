package com.creditease.ns4.gear.watchdog.monitor.jmx;

import javax.management.ObjectName;

/**
 * @author yaqiangzhao
 * 2019/1/15
 */
public interface ManagedObjectMXBean {
    /**
     * 获取MBean的ObjectName
     *
     * @return
     */
    public ObjectName getObjectName();

    /**
     * 获取MBean的名称
     *
     * @return
     */
    public String getName();

    /**
     * 获取MBean的类型
     *
     * @return
     */
    public String getType();
}
