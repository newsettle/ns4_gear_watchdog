package com.creditease.ns4.gear.watchdog;

import com.creditease.ns4.gear.watchdog.monitor.collect.jvm.JVMCollectService;

public class CollectTest {
    public static void main(String[] args) {
        JVMCollectService service = new JVMCollectService();
        service.prepare();
        service.boot();
    }
}
