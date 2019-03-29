package com.creditease.ns4.gear.watchdog.common.jvm.report;

import com.creditease.ns4.gear.watchdog.common.jvm.report.monitors.ProcessMonitor;

import java.util.Map;

/**
 * @author outman
 * @description Process Report
 * @date 2019/3/5
 */
public class ProcessReport {
    private ProcessReport() { }

    /**
     * Build a report with current Process information
     * @return a Map with the current process report
     */
    public static Map<String, Object> generate() {
        return ProcessMonitor.detect().toMap();
    }
}
