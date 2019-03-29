package com.creditease.ns4.gear.watchdog.monitor.jmx;

/**
 * @ClassName MonitorDataMXBean
 * @Author karma
 * @Date 2019/1/23 下午2:51
 * @Version 1.0
 **/
public interface NSBusinessMXBean extends ManagedObjectMXBean {

    public String getNsSendQueueBuffer();

    public String getNsReceiveQueueBuffer();

    public  String getNsTradeMessageBuffer();
}
