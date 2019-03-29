package com.creditease.ns4.gear.watchdog.common.log;

import com.creditease.ns4.gear.watchdog.common.DateUtil;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author outman
 * @description JDK日志包装
 * @date 2019/1/15
 */
public class LogFactory {

    /**
     * 全局Log的名称
     */
    public static final String LOG_NAME = "Global";
    /**
     * 静态变量globleLog
     */
    private static Logger globalLog;

    /**
     * 初始化全局Logger
     *
     * @return
     */
    private static void initGlobalLog(String logPath) {
        // 获取Log
        Logger log = Logger.getLogger(LOG_NAME);
        // 为log设置全局等级
        log.setLevel(Level.ALL);
        Handler[] hs = log.getHandlers();
        for (Handler h : hs) {
            h.close();
            log.removeHandler(h);
        }
        // 添加控制台handler
        //LogUtil.addConsoleHandler(log, Level.INFO);
        // 添加文件输出handler
        LogUtil.addFileHandler(log, Level.INFO, logPath);
        // 设置不适用父类的handlers，这样不会在控制台重复输出信息
        log.setUseParentHandlers(false);
        globalLog = log;
    }

    public static Logger getGlobalLog() {
        return globalLog;
    }

    public static void initLog(String logFilePath, String logFileName) {
        StringBuilder logPathBuilder = new StringBuilder(logFilePath);
        File logFilePathFile = new File(logFilePath);
        if (!logFilePathFile.exists()) {
            logFilePathFile.mkdirs();
        }
        logPathBuilder.append(File.separator).append(File.separator);
        logPathBuilder.append(logFileName).append("_").append(DateUtil.getCurrentDate(DateUtil.DATE_PATTERN_NOMARK)).append(".log");
        initGlobalLog(logPathBuilder.toString());
    }
}
