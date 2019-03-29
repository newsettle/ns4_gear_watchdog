package com.creditease.ns4.gear.watchdog.monitor.jmx.server.service;

import com.creditease.ns4.gear.watchdog.common.DateUtil;
import com.creditease.ns4.gear.watchdog.monitor.WatchdogServer;
import com.creditease.ns4.gear.watchdog.monitor.jmx.AbstractManagedObject;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;
import com.creditease.ns4.gear.watchdog.monitor.jmx.WatchDogServiceMXBean;
import com.creditease.ns4.gear.watchdog.monitor.jmx.constant.JMX;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.MemoryUsage;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description watchwog service
 * @Author karma
 * @Date 2019/1/21 下午7:18
 * @Version 1.0
 **/
public class WatchDogService extends AbstractManagedObject
        implements WatchDogServiceMXBean {

    public MBeanServerConnection mbsc;

    public WatchDogService(Jmx jmx) {
        this(jmx, null, false);
    }

    public WatchDogService(Jmx jmx, String name, boolean isChildProcessMXBean) {
        super(jmx, name, isChildProcessMXBean);
        try {
            mbsc = this.jmx.getMbServerConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private long getProcessCpuTime() {
        try {
            return (long)mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "ProcessCpuTime");
        } catch (Exception ex) {}
        return System.currentTimeMillis();
    }

    @Override
    public long getTotalLoadedClassCount() {
        try {
            return (long) mbsc.getAttribute(new ObjectName("java.lang:type=ClassLoading"), "TotalLoadedClassCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getLoadedClassCount() {
        try {
            return (int) mbsc.getAttribute(new ObjectName("java.lang:type=ClassLoading"), "LoadedClassCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getUnloadedClassCount() {
        try {
            return (int) mbsc.getAttribute(new ObjectName("java.lang:type=ClassLoading"), "UnloadedClassCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getCollectionCount() {
        try {
            return (long) mbsc.getAttribute(new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge"), "CollectionCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getCollectionTime() {
        try {
            return (long) mbsc.getAttribute(new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge"), "CollectionTime");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public int getObjectPendingFinalizationCount() {
        try {
            return (int) mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "ObjectPendingFinalizationCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public MemoryUsage getHeapMemoryUsage() {
        try {
            Object data = mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
            if (data instanceof MemoryUsage) {
                return (MemoryUsage) data;
            } else if (data instanceof CompositeData) {
                return MemoryUsage.from((CompositeData) data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MemoryUsage getNonHeapMemoryUsage() {
        try {
            Object data = mbsc.getAttribute(new ObjectName("java.lang:type=Memory"), "NonHeapMemoryUsage");
            if (data instanceof MemoryUsage) {
                return (MemoryUsage) data;
            } else if (data instanceof CompositeData) {
                return MemoryUsage.from((CompositeData) data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MemoryUsage getUsage() {
        try {
            Object data = mbsc.getAttribute(new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"), "Usage");
            if (data instanceof MemoryUsage) {
                return (MemoryUsage) data;
            } else if (data instanceof CompositeData) {
                return MemoryUsage.from((CompositeData) data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MemoryUsage getPeakUsage() {
        try {
            Object data = mbsc.getAttribute(new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"), "PeakUsage");
            if (data instanceof MemoryUsage) {
                return (MemoryUsage) data;
            } else if (data instanceof CompositeData) {
                return MemoryUsage.from((CompositeData) data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public MemoryUsage getCollectionUsage() {
        try {
            Object data = mbsc.getAttribute(new ObjectName("java.lang:type=MemoryPool,name=PS Old Gen"), "CollectionUsage");
            if (data instanceof MemoryUsage) {
                return (MemoryUsage) data;
            } else if (data instanceof CompositeData) {
                return MemoryUsage.from((CompositeData) data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public String getArch() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "Arch");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public String getVersion() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "Version");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public int getAvailableProcessors() {
        try {
            return (int) mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "AvailableProcessors");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public int getThreadCount() {
        try {
            return (int) mbsc.getAttribute(new ObjectName("java.lang:type=Threading"), "ThreadCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public long getTotalStartedThreadCount() {
        try {
            return (long) mbsc.getAttribute(new ObjectName("java.lang:type=Threading"), "TotalStartedThreadCount");
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public String getUptime() {
        long time = (System.currentTimeMillis() - getStartDateTime()) / JMX.SECOND_TIME;

        long hourTime = time / JMX.HOUR_TIME;
        long minTime = (time - hourTime * JMX.HOUR_TIME) / JMX.MINUTE_TIME;
        long secondTime = (time - hourTime * JMX.HOUR_TIME) % JMX.MINUTE_TIME;

        StringBuffer sb = new StringBuffer();
        sb.append(hourTime).append("H").append(" ").append(minTime).append("m").append(" ").append(secondTime).append("s");
        return sb.toString();
    }

    @Override
    public String getState() {
        if (isChildProcessMXBean) {
            return WatchdogServer.instance().getWatchdogChildTask().getProcessInfo().getStatus().getCode() == 1 ? "ACTIVE" : "STOP";
        } else {
            return "ACTIVE";
        }
    }

    private long getStartDateTime() {
        if (isChildProcessMXBean) {
            if (isChildProcessMXBean && WatchdogServer.instance().getWatchdogChildTask().getProcessInfo().getStatus().getCode() != 1) {
                return System.currentTimeMillis();
            }
            return WatchdogServer.instance().getWatchdogChildTask().getProcessInfo().getStartTime();
        } else {
            try {
                return (long) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "StartTime");
            } catch (Exception ex) {
            }
        }
        return 0;
    }

    @Override
    public String getStartTime() {
        long startDateTime = this.getStartDateTime();
        if (startDateTime != 0) {
            return DateUtil.timeToStr(startDateTime);
        }
        return "NONE";
    }

    @Override
    public String getStopTime() {
        if (isChildProcessMXBean) {
            long stop = WatchdogServer.instance().getWatchdogChildTask().getProcessInfo().getStopTime();
            if (stop != 0) {
                return DateUtil.timeToStr(stop);
            }
        } else {
        }
        return "NONE";
    }

    @Override
    public String getClassPath() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "ClassPath");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public String getLibraryPath() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "LibraryPath");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public String getBootClassPath() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "BootClassPath");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public String getInputArguments() {
        try {
            String[] args = (String[]) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "InputArguments");
            StringBuffer sb = new StringBuffer();
            for (String arg : args) {
                sb.append(arg);
            }
            return sb.toString();
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public String getVmName() {
        try {
            return (String) mbsc.getAttribute(new ObjectName("java.lang:type=Runtime"), "VmName");
        } catch (Exception e) {
        }
        return "NONE";
    }

    @Override
    public void start() throws Exception {
        if (isChildProcessMXBean) {
            WatchdogServer.instance().getWatchdogChildTask().start();
        }
    }

    @Override
    public void stop() throws Exception {
        if (isChildProcessMXBean) {
            WatchdogServer.instance().getWatchdogChildTask().stop();
        }
    }

    @Override
    public void restart() throws Exception {
        if (isChildProcessMXBean) {
            WatchdogServer.instance().getWatchdogChildTask().restart();
        }
    }

    @Override
    public String checkCpuRatio() {
        try {
            long cpuStartTime = System.currentTimeMillis();
            long cpuStartTTime = getProcessCpuTime();
            try {
                Thread.sleep(5000);
            } catch (Exception ex) {}
            long end = System.currentTimeMillis();
            long endT = getProcessCpuTime();
            int availableProcessors = (int)mbsc.getAttribute(new ObjectName("java.lang:type=OperatingSystem"), "AvailableProcessors");
            double ratio = (endT - cpuStartTTime) / 1000000.0 / (end - cpuStartTime) / availableProcessors;
            BigDecimal bg = new BigDecimal(ratio * 100).setScale(2, RoundingMode.DOWN);
            return "CPU Ratio: "+bg.toString() + "%";
        } catch (Exception e) {
        }
        return "CPU Ratio: NONE";
    }

}
