package com.creditease.ns4.gear.watchdog.monitor.jmx;

import java.lang.management.MemoryUsage;

/**
 * @ClassName NSService
 * @Description TODO
 * @Author karma
 * @Date 2019/1/21 下午7:08
 * @Version 1.0
 **/
public interface WatchDogServiceMXBean extends ManagedObjectMXBean {

    /**
     *@Description 获取已加载的类总数
     *@return long
     */
    public long getTotalLoadedClassCount();

    /**
     *@Description 获取已加载的当前类数量
     *@return long
     */
    public long getLoadedClassCount();

    /**
     *@Description 得到卸载的类总数
     *@return long
     */
    public long getUnloadedClassCount();

    /***
     *@Description 得到运行时间
     *@return java.lang.String
     */
    public String getUptime();

    /***
     *@Description 得到运行状态
     *@return java.lang.String
     */
    public String getState();

    /***
     *@Description 获得进程的启动时间
     *@return java.lang.String
     */
    public String getStartTime();

    /***
     *@Description 获得进程的停止时间
     *@return java.lang.String
     */
    public String getStopTime();

    /***
     *@Description 得到垃圾收集器收集的数量
     *@return long
     */
    public long getCollectionCount();

    /***
     *@Description 得到垃圾收集器总花费的时间
     *@return long
     */
    public long getCollectionTime();

    /***
     *@Description 返回其终止被挂起的对象的近似数目
     *@return int
     */
    public int getObjectPendingFinalizationCount();
    
    /***
     *@Description 获取堆内容的使用情况
     *@return java.lang.management.MemoryUsage
     */
    public MemoryUsage getHeapMemoryUsage();

    /***
     *@Description 获取非堆内容的使用情况
     *@return java.lang.management.MemoryUsage
     */
    public MemoryUsage getNonHeapMemoryUsage();

    /***
     *@Description 内存池当前使用量的估计数。对于垃圾回收内存池，已使用的内存包括由池中所有对象（包括可到达 对象和不可达到 对象）占用的内存。
     *@return java.lang.management.MemoryUsage
     */
    public MemoryUsage getUsage();

    /***
     *@Description 返回自 java 虚拟机启动以来或自峰值重置以来此内存池的峰值内存使用量。如果此内存池无效（即不再存在），此方法将返回 null。
     *@return java.lang.management.MemoryUsage
     */
    public MemoryUsage getPeakUsage();

    /***
     *@Description 返回 Java 虚拟机最近回收了此内存池中的不使用的对象之后的内存使用量。
     *@return java.lang.management.MemoryUsage
     */
    public MemoryUsage getCollectionUsage();

    /***
     *@Description
     *@return java.lang.String
     */
    public String getArch();

    /***
     *@Description 获取操作系统版本号
     *@return java.lang.String
     */
    public String getVersion();

    /***
     *@Description 获得可用处理器的个数
     *@return int
     */
    public int getAvailableProcessors();

    /***
     *@Description 返回活动线程的当前数目，包括守护线程和非守护线程。
     *@return int
     */
    public int getThreadCount();

    /***
     *@Description 返回自从 Java 虚拟机启动以来创建和启动的线程总数目
     *@return long
     */
    public long getTotalStartedThreadCount();

    /***
     *@Description 返回系统类加载器用于搜索类文件的 Java 类路径
     *@return java.lang.String
     */
    public String getClassPath();
    
    /***
     *@Description 返回 Java 库路径
     *@return java.lang.String
     */
    public String getLibraryPath();

    /***
     *@Description 返回由引导类加载器用于搜索类文件的引导类路径。
     *@return java.lang.String
     */
    public String getBootClassPath();

    /***
     *@Description 获取JVM输入参数
     *@return java.lang.String
     */
    public String getInputArguments();

    /***
     *@Description 获取java虚拟机名称
     *@return java.lang.String
     */
    public String getVmName();

    /***
     *@Description 当前cpu的占用率
     *@return java.lang.String
     */
    public String checkCpuRatio();

    /***
     *@Description 启动进程
     *@return void
     */
    public void start() throws Exception;

    /***
     *@Description 停止进程
     *@return void
     */
    public void stop() throws Exception;

    /***
     *@Description 重启进程
     *@return void
     */
    public void restart() throws Exception;

}
