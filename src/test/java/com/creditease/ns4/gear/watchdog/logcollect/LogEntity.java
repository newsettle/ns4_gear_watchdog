package com.creditease.ns4.gear.watchdog.logcollect;

public class LogEntity {
    private String logName;
    private String logSrc;

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getLogSrc() {
        return logSrc;
    }

    public void setLogSrc(String logSrc) {
        this.logSrc = logSrc;
    }

    @Override
    public String toString() {
        return "LogEntity{" +
                "logName='" + logName + '\'' +
                ", logSrc='" + logSrc + '\'' +
                '}';
    }
}
