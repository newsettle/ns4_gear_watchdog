package com.creditease.ns4.gear.watchdog.jmx;

import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;

public class HelloAgent {
    public static void main(String[] args) throws Exception {
        Jmx.getInstance(null).start();
    }
}
