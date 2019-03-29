package com.creditease.ns4.gear.watchdog.common.log;

import com.creditease.ns4.gear.watchdog.common.DateUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * @author outman
 * @description JDK日志工具辅助类
 * @date 2019/1/15
 */
public class LogUtil {

    /**
     * 日志输出元素分割符
     */
    public static final String LOG_SEPARATOR = " ";
    /**
     * 分隔符
     */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");


    /**
     * 为log设置等级
     *
     * @param log
     * @param level
     */
    public static void setLogLevel(Logger log, Level level) {
        log.setLevel(level);
    }

    /**
     * 为log添加控制台handler
     *
     * @param log   要添加handler的log
     * @param level 控制台的输出等级
     */
    public static void addConsoleHandler(Logger log, Level level) {
        // 控制台输出的handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        // 设置控制台输出的等级（如果ConsoleHandler的等级高于或者等于log的level，则按照FileHandler的level输出到控制台，如果低于，则按照Log等级输出）
        consoleHandler.setLevel(level);
        // 添加控制台的handler
        log.addHandler(consoleHandler);
    }

    /**
     * 为log添加文件输出Handler
     *
     * @param log      要添加文件输出handler的log
     * @param level    log输出等级
     * @param filePath 指定文件全路径
     */
    public static void addFileHandler(Logger log, Level level, String filePath) {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(filePath);
            // 设置输出文件的等级（如果FileHandler的等级高于或者等于log的level，则按照FileHandler的level输出到文件，如果低于，则按照Log等级输出）
            fileHandler.setLevel(level);
            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    // 设置文件输出格式
                    StringBuilder builder = new StringBuilder();
                    builder.append(DateUtil.getCurrentDate(DateUtil.DATE_PATTERN_FULL)).append(LOG_SEPARATOR);
                    builder.append("[").append(record.getLevel().getName().substring(0,1)).append("]").append(LOG_SEPARATOR);
                    builder.append("[").append(record.getSourceClassName()).append(".").append(record.getSourceMethodName()).append("()").append("]").append(LOG_SEPARATOR);
                    builder.append(record.getMessage());
                    builder.append(LINE_SEPARATOR);
                    if (record.getThrown() != null) {
                        try {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            record.getThrown().printStackTrace(pw);
                            pw.close();
                            builder.append(sw.toString());
                        } catch (Exception ex) {
                        }
                    }
                    return builder.toString();
                }
            });
        } catch (SecurityException e) {
        } catch (IOException e) {
        }
        if (fileHandler != null) {
            // 添加输出文件handler
            log.addHandler(fileHandler);
        }
    }
}
