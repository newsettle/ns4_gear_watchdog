package com.creditease.ns4.gear.watchdog.common.monitor;

/**
 * @author outman
 * @description 定义监控数据类型枚举
 * @date 2019/1/15
 */
public enum MessageType {

    /**
     * NS框架发送队列
     */
    NS_SEND_QUEUE_BUFFER("NS_SendQueueBuffer"),
    /**
     * NS框架接收队列
     */
    NS_RECEIVE_QUEUE_BUFFER("NS_ReceiveQueueBuffer"),
    /**
     * NS框架接收y业务数据队列
     */
    NS_TRADE_MESSAGE("NS_Trademessage");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
