package com.creditease.ns4.gear.watchdog.monitor.jmx;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.JmxUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName AbstractManagedObject
 * @Author karma
 * @Date 2019/1/15 下午6:28
 * @Version 1.0
 **/
public abstract class AbstractManagedObject implements ManagedObjectMXBean {

    private static final NsLog logger = NsLogger.getWatchdogLogger();

    private String name;
    private ObjectName objectName;
    protected Jmx jmx;
    protected boolean isChildProcessMXBean;

    public AbstractManagedObject(Jmx jmx, String name, boolean isChildProcessMXBean) {
        this.jmx = jmx;
        this.name = name;
        this.isChildProcessMXBean = isChildProcessMXBean;
        this.registerSelf();
    }

    @Override
    public ObjectName getObjectName() {
        if (this.objectName == null) {
            try {
                Map<String, String> props = new LinkedHashMap<String, String>();
                props.put("type", this.getType());
                String name = this.name;
                if (name != null) {
                    if (name.indexOf(58) >= 0) {
                        name = ObjectName.quote(name);
                    }
                    props.put("name", name);
                }
                this.objectName = Jmx.getObjectName(JmxUtil.DOMAIN, props);
            } catch (MalformedObjectNameException var3) {
                throw new RuntimeException(var3);
            }
        }

        return this.objectName;
    }

    @Override
    public String getType() {
        Class<?>[] interfaces = this.getClass().getInterfaces();
        int p;
        for (p = 0; p < interfaces.length; ++p) {
            String className = interfaces[p].getName();
            if (className.endsWith("MXBean")) {
                int lidx = className.lastIndexOf(46);
                int idx = className.indexOf("MXBean");
                return className.substring(lidx + 1, idx);
            }
        }
        p = this.getClass().getName().lastIndexOf(46);
        return this.getClass().getName().substring(p + 1);
    }

    protected boolean registerSelf() {
        try {
            jmx.register(this, this.getObjectName());
            return true;
        } catch (RuntimeException rex) {
            throw rex;
        } catch (Exception ex) {
            logger.error("registerSelf ERROR:{} {}", ex.toString(), ex);
            return false;
        }
    }

    protected boolean unregisterSelf() {
        try {
            jmx.unregister(this.getObjectName());
            return true;
        } catch (Throwable var2) {
            logger.error("unregisterSelf ERROR:{} {}", var2.toString(), var2);
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.getObjectName() + "]";
    }

    @Override
    public String getName() {
        return name;
    }
}