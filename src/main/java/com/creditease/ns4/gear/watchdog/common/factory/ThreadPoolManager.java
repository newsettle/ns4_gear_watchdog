package com.creditease.ns4.gear.watchdog.common.factory;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author outman
 * @description 线程池管理
 * @date 2019/2/26
 */
public class ThreadPoolManager {
    private static final NsLog logger = NsLogger.getWatchdogLogger();
    private static ThreadPoolManager instance = new ThreadPoolManager();
    /**
     * 存放线程池们
     */
    private Map<String, ExecutorService> threadPools = new ConcurrentHashMap<>();

    public static ThreadPoolManager getInstance() {
        return instance;
    }

    /**
     * 创建固定下线程数线程池
     *
     * @param threadName 线程名字
     * @param poolSize   线程池大小
     * @return 线程池
     * @author outman
     */
    public ExecutorService newFixedThreadPool(String threadName, int poolSize) {
        return Executors.newFixedThreadPool(poolSize, new ThreadFactoryBuilder().setNameFormat(threadName).build());
    }

    /**
     * 创建 ThreadPoolExecutor 线程池
     *
     * @param threadName         线程池名称[功能名-d%]
     * @param corePoolSize       核心线程数
     * @param maximumPoolSize    最大线程数
     * @param keepAliveTime      线程空闲时间
     * @param unit               活动时间
     * @param blockQueueCapacity 工作队列长度 默认实现类型 -> LinkedBlockingQueue
     * @return ExecutorService
     * @author 杨红岩
     */
    public ExecutorService newExecutorService(String threadName, int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit, int blockQueueCapacity) throws Exception {
        return this.newExecutorService(threadName, corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(blockQueueCapacity));
    }

    /**
     * 创建 ThreadPoolExecutor 线程池
     *
     * @param threadName         线程池名称[功能名-d%]
     * @param corePoolSize       核心线程数
     * @param maximumPoolSize    最大线程数
     * @param keepAliveTime      线程空闲时间
     * @param unit               活动时间
     * @param blockQueueCapacity 工作队列长度 默认实现类型 -> LinkedBlockingQueue
     * @param handler            线程池处理策略
     * @return ExecutorService
     * @author outman
     */
    public ExecutorService newExecutorService(String threadName, int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit, int blockQueueCapacity, RejectedExecutionHandler handler) throws Exception {
        return this.newExecutorService(threadName, corePoolSize, maximumPoolSize, keepAliveTime, unit,
                new LinkedBlockingQueue<Runnable>(blockQueueCapacity), handler);
    }

    /**
     * 创建 ThreadPoolExecutor 线程池
     *
     * @param threadName      线程池名称[功能名-d%]
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   线程空闲时间
     * @param unit            活动时间
     * @param workQueue       工作队列
     * @return ExecutorService
     * @author 杨红岩
     */
    public ExecutorService newExecutorService(String threadName, int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit, BlockingQueue<Runnable> workQueue) throws Exception {
        return this.newExecutorService(threadName, corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * 创建 ThreadPoolExecutor 线程池
     *
     * @param threadName      线程池名称[功能名-d%]
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   线程空闲时间
     * @param unit            活动时间
     * @param workQueue       工作队列
     * @param handler         线程池处理策略
     * @return ExecutorService
     * @author 杨红岩
     */
    public ExecutorService newExecutorService(String threadName, int corePoolSize,
                                              int maximumPoolSize,
                                              long keepAliveTime,
                                              TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) throws Exception {
        if (threadName == null) {
            throw new NullPointerException("线程名称为空");
        } else if (this.threadPools.containsKey(threadName) && !this.threadPools.get(threadName).isShutdown()) {
            //线程名称存在跑出异常
            throw new Exception("线程名称" + threadName + "已经存在,并且是运行状态");
        }
        ExecutorService executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, new ThreadFactoryBuilder().setNameFormat(threadName).build(), handler);
        this.threadPools.put(threadName, executorService);
        return executorService;
    }

    /**
     * 创建 ScheduledExecutorService 线程池
     *
     * @param threadName   线程池名称
     * @param corePoolSize 核心线程数
     * @return ScheduledExecutorService
     * @author 杨红岩
     */
    public ScheduledExecutorService newScheduledExecutorService(String threadName, int corePoolSize) throws Exception {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactoryBuilder().setNameFormat(threadName).build());
        if (threadName == null) {
            throw new NullPointerException("线程名称为空");
        } else if (this.threadPools.containsKey(threadName) && !this.threadPools.get(threadName).isShutdown()) {
            //线程名称存在跑出异常
            throw new Exception("线程名称" + threadName + "已经存在,并且是运行状态");
        }
        this.threadPools.put(threadName, scheduledExecutorService);
        return scheduledExecutorService;
    }

    /**
     * 关闭所有线程池
     * 停止进程时 kill -15 pid 调用此方法
     *
     * @author 杨红岩
     */
    public void shutdown() {
        Map<String, ExecutorService> threadPools = this.threadPools;
        logger.info(" ---准备---关闭所有线程池，线程池对象数量：{}", threadPools.size());
        for (Map.Entry<String, ExecutorService> me : threadPools.entrySet()) {
            logger.info("关闭[{}]线程池-开始,关闭前线程状态-[isShutdown:{}, isTerminated:{}]",
                    me.getKey(), me.getValue().isShutdown(), me.getValue().isTerminated());
            me.getValue().shutdown();
            logger.info("关闭[{}]线程池-结束,关闭后线程状态-[isShutdown:{}, isTerminated:{}]",
                    me.getKey(), me.getValue().isShutdown(), me.getValue().isTerminated());
        }
        logger.info(" ---结束---关闭所有线程池，线程池对象数量：{}", threadPools.size());
        logger.info("清空线程池集合开始");
        this.threadPools.clear();
        logger.info("清空线程池集合结束");
    }

    /**
     * 停止指定的线程
     * 代码中停止指定的线程
     *
     * @author 杨红岩
     */
    public void shutdown(ExecutorService threadPool) {
        if (threadPool != null) {
            if (!threadPool.isShutdown()) {
                threadPool.shutdownNow();
            }
        }
    }
}


