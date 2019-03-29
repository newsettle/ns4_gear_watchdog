package com.creditease.ns4.gear.watchdog;

import com.creditease.ns4.gear.watchdog.common.monitor.MessageType;

public class EnumTest {
    public static void main(String[] args) {
        System.out.println(MessageType.NS_RECEIVE_QUEUE_BUFFER);
        System.out.println(MessageType.NS_RECEIVE_QUEUE_BUFFER.toString());
        System.out.println(MessageType.valueOf(MessageType.NS_RECEIVE_QUEUE_BUFFER.name()));
        System.out.println(System.getProperty("user.dir"));
    }
}
