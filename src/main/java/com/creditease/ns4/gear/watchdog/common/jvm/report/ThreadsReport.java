package com.creditease.ns4.gear.watchdog.common.jvm.report;

import com.creditease.ns4.gear.watchdog.common.jvm.report.monitors.HotThreadsMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author outman
 * @description A ThreadsReport object used to hold the hot threads information
 * @date 2019/3/5
 */
public class ThreadsReport {


    /**
     * Generate a report with current Thread information
     * @param options Map of options to narrow this method functionality:
     *                Keys: ordered_by - can be "cpu", "wait" or "block"
     *                      stacktrace_size - max depth of stack trace
     * @return A Map containing hot threads information
     */
    public static List<Map<String, Object>> generate(Map<String, String> options) {
        List<HotThreadsMonitor.ThreadReport> reports = HotThreadsMonitor.detect(options);
        List<Map<String,Object>> reportsList = new ArrayList<Map<String,Object>>();
        for(HotThreadsMonitor.ThreadReport report : reports) {
            reportsList.add(report.toMap());
        }
        return reportsList;
    }


    /**
     * Generate a report with current Thread information
     * @return A Map containing the hot threads information
     */
    public static List<Map<String, Object>> generate() {
        Map<String, String> options = new HashMap<>();
        options.put("order_by", "cpu");
        return generate(options);
    }
}

