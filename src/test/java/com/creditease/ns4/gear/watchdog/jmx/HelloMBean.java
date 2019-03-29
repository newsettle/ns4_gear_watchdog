package com.creditease.ns4.gear.watchdog.jmx;

public interface HelloMBean {
    public String getName();

    public void setName(String name);

    public void helloWorld();

    public void helloWorld(String name);

    public String getTelephone();
}
