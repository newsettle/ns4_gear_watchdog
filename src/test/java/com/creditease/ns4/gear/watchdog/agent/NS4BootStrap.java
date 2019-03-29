package com.creditease.ns4.gear.watchdog.agent;

public class NS4BootStrap {

    public static void main(String[] args) {
        System.out.println("NS4BootStrap");
        new NS4BootStrap().sayHello();
    }

    public void sayHello() {
        System.out.println("Hello, guys!");
    }
}
