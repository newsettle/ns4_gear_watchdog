package com.creditease.ns4.gear.watchdog.monitor.es;

public class MonitorEventSearchParam {

    private String name;
    private String msgId;
    private String eventType;
    private String queueName;
    private long timeHappend;
    private String traMessage;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public long getTimeHappend() {
        return timeHappend;
    }

    public void setTimeHappend(long timeHappend) {
        this.timeHappend = timeHappend;
    }

    public String getTraMessage() {
        return traMessage;
    }

    public void setTraMessage(String traMessage) {
        this.traMessage = traMessage;
    }
}
