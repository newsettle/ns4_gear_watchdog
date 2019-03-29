package com.creditease.ns4.gear.watchdog.monitor.collect.log.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Map;

/**
 * @author outman
 * @description 日志实体类
 * @date 2019/2/28
 */
public class LogEntity implements Serializable {

    /**系统时间*/
    @JSONField(name = "@timestamp")
    private String timestamp;
    /**日志时间*/
    @JSONField(name = "logtime")
    private String logTime;
    /**日志源*/
    private String source;
    /**日志内容*/
    private String message;
    /**迎合日志查询组件，定义机器属性字段*/
    private Map<String,String> beat;
    /**行数*/
    private long offset;
    /**行数*/
    private long lineNum;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getBeat() {
        return beat;
    }

    public void setBeat(Map<String, String> beat) {
        this.beat = beat;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLineNum() {
        return lineNum;
    }

    public void setLineNum(long lineNum) {
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return "LogEntity{" +
                "timestamp='" + timestamp + '\'' +
                ", logTime='" + logTime + '\'' +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", beat=" + beat +
                ", offset=" + offset +
                ", lineNum=" + lineNum +
                '}';
    }
}
