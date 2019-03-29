package com.creditease.ns4.gear.watchdog.common;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import java.net.*;
import java.util.Enumeration;

/**
 * @author outman
 * @description IP工具类
 * @date 2019/1/15
 */
public class IPUtil {

    private static final NsLog logger = NsLogger.getWatchdogLogger();

    /**
     * 获取IP地址
     *
     * @param preferIpv4
     * @param preferIPv6
     * @return IP地址
     */
    public static String getHostAddress(boolean preferIpv4, boolean preferIPv6) {
        String hostAddress = null;
        try {
            hostAddress = IPUtil.getFirstNonLoopbackAddress(preferIpv4, preferIPv6).getHostAddress();
        } catch (SocketException e) {
            logger.error("获取本机IP异常-RUN：{} {}", e.getMessage(), e);
        }
        return hostAddress;
    }

    /**
     * 获取Windows或者linux系统的本机服务IP
     *
     * @param preferIpv4
     * @param preferIPv6
     * @return
     * @throws SocketException
     */
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
        Enumeration en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = (NetworkInterface) en.nextElement();
            for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                InetAddress addr = (InetAddress) en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        if (preferIPv6) {
                            continue;
                        }
                        return addr;
                    }
                    if (addr instanceof Inet6Address) {
                        if (preferIpv4) {
                            continue;
                        }
                        return addr;
                    }
                }
            }
        }
        return null;
    }
}
