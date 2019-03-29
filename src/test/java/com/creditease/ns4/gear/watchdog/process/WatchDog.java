package com.creditease.ns4.gear.watchdog.process;

import com.creditease.ns4.gear.watchdog.monitor.WatchdogServer;

public class WatchDog {

    public static void main(String[] args) {
        /**
         * 命令行参数说明：
         *  -sm 服务启动入口，值固定->NS4框架启动服务类【NSDispatcher | NSTransporter】
         *  示例：-sm NSDispatcher or -sm NSTransporter
         *  -an 应用名称，如果没有使用默认名称
         *  示例：-an watchDogDemo
         */
        WatchdogServer.main(args);
    }
}