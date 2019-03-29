package com.creditease.ns4.gear.watchdog;

import com.creditease.ns4.gear.watchdog.common.monitor.LimitQueue;

public class LimitQueueTest {
    public static void main(String[] args) {
        LimitQueue<String> limitQueue = new LimitQueue<String>(2);
        limitQueue.offer("111");
        limitQueue.offer("222");
        while (limitQueue.size() != 0) {
            System.out.println(limitQueue.poll());
        }
    }
}
