package com.creditease.ns4.gear.watchdog.jmx;

public class Hello implements HelloMBean {

    private String name;

    public Hello() {
    }

    public Hello(String pid) {
        System.out.println(pid);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void helloWorld() {
        System.out.println(this.name + " helloWorld");
    }

    @Override
    public void helloWorld(String name) {
        this.name = "helloWorld " + name;
    }

    @Override
    public String getTelephone() {
        return "getTelephone";
    }
}
