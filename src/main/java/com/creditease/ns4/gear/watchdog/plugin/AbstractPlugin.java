package com.creditease.ns4.gear.watchdog.plugin;

import com.creditease.framework.ext.plugin.Plugin;

import java.util.Properties;

/**
 * @author outman
 * @description Watchdog集成NS的插件基本实现
 * @date 2019/1/25
 */
public abstract class AbstractPlugin implements Plugin {

    protected String name;

    protected boolean loaded;

    protected Properties properties;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void register(String name, Properties props) {
        //这里做预加载
        this.name = name;
        this.properties = props;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }
}
