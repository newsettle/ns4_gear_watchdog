package com.creditease.ns4.gear.watchdog.common;


import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadPoolManager;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author outman
 * @description 进程间通讯辅助类
 * @date 2019/1/15
 */
public class PingUtil {

    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();

    /**
     * ping线程池
     */
    private static ExecutorService pingPool = null;

    public static void ping(Object[] args) {
        logger.info("child ping 启动,开始动态获取通讯的port");
        final int port = CommandLine.parse(args).getPingPort();
        logger.info("child ping port:[{}]", port);

        if (port == 0) {
            logger.error("端口怎么能是0呢，退出吧，老铁");
            System.exit(0);
        }
        if (pingPool == null) {
            try {
                pingPool = ThreadPoolManager.getInstance().newExecutorService("ping-%d", 1, 1, 0L, TimeUnit.MILLISECONDS, 1);
            } catch (Exception e) {
                logger.error("创建ping线程池错误：{}", e.getMessage());
            }
        }
        pingPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("与[{}]端口进行ping", port);
                Socket socket = null;
                try {
                    socket = new Socket("127.0.0.1", port);
                    InputStream s = socket.getInputStream();
                    byte[] buf = new byte[1024];
                    int len = 0;
                    while ((len = s.read(buf)) != -1) {
                        logger.info("ping " + new String(buf, 0, len));
                    }
                    logger.info("与[{}]端口进行ping结束", port);
                } catch (IOException e) {
                    logger.error("与[{}]端口进行ping发生异常 {}", port, e);
                } finally {
                    logger.info("与[{}]端口进行ping结束,执行finally释放资源", port);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        logger.error("socket close IOException: {} {}", e.getMessage(), e);
                    }
                    logger.info("与[{}]端口进行ping结束,执行finally释放资源,退出子进程", port);
                    System.exit(0);
                }
            }
        });
    }

    public static void clear() {
        if (pingPool != null) {
            pingPool.shutdown();
            pingPool = null;
        }
    }
}
