package com.creditease.ns4.gear.watchdog.monitor.collect;

/**
 * @author outman
 * @description 归集service
 * @date 2019/2/27
 */
public interface CollectService {

    void prepare();

    void boot();

    void onComplete();

    void shutdown();
}
