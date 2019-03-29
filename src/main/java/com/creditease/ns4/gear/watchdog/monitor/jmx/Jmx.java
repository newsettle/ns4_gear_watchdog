package com.creditease.ns4.gear.watchdog.monitor.jmx;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.JmxUtil;
import com.creditease.ns4.gear.watchdog.common.PackageUtil;
import com.creditease.ns4.gear.watchdog.common.ProcessUtil;
import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.jvm.ProcessJmx;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.monitor.jmx.server.service.WatchDogService;
import com.sun.jdmk.comm.HtmlAdaptorServer;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @ClassName Jmx
 * @Description
 * @Author karma
 * @Date 2019/1/15 下午6:49
 * @Version 1.0
 **/
public class Jmx {

    private static final NsLog logger = NsLogger.getWatchdogLogger();
    private MBeanServerConnection mbServerConnection;
    private MBeanServer mbServer = ManagementFactory.getPlatformMBeanServer();
    private ArrayList<AbstractManagedObject> mbeanList = new ArrayList<AbstractManagedObject>();
    private static Map<String, Jmx> instances = new HashMap<String, Jmx>();

    public String pid;
    //public static Map<String, Long> checkerEnvMap = new HashMap<String, Long>();
    public static Pattern escapePattern = Pattern.compile("[,=:\"*?]");

    /**
     * 子进程jmx实例
     */
    private Jmx childJmx;

    private ProcessJmx processJmx;

    private Jmx(String pid) {
        this.pid = pid;
    }

    public static Jmx getInstance() {
        return Jmx.getInstance(null);
    }

    public static Jmx getInstance(String pid) {
        if (pid == null) {
            pid = ProcessUtil.getProcessID() + "";
        }
        if (instances.get(pid) == null) {
            Jmx jmx = new Jmx(pid);
            instances.put(pid, jmx);
        }
        return instances.get(pid);
    }

    public static void clearJmx(String pid) {
        if (pid == null) {
            return;
        }
        instances.put(pid, null);
    }

//    public int getPort() {
//        return PropertiesUtil.getInteger("jmx.registry.port");
//    }

    public int getHtmlAdaptorPort() {
        return PropertiesUtil.getInteger("jmx.htmlAdaptor.port");
    }

    /**
     * 1.先启动自身的jmx服务（注册自身的mxbean）
     * 2.判断有没有子进程启动
     * 3.如果有子进程，则对应启动子进程的jmx服务
     * 4.在主进程中注册调用子进程的mxbean
     *
     * @return boolean
     * @date 2019/1/22 上午10:50
     * @author karma
     */
    public boolean start() {
        if (mbServerConnection != null) {
            return false;
        }
        try {
            //这句话非常重要，不能缺少！注册一个端口，绑定url后，客户端就可以使用rmi通过url方式来连接JMXConnectorServer
//            Registry registry = LocateRegistry.createRegistry(getPort());
//            //构造JMXServiceURL
//            String url = "service:jmx:rmi:///jndi/rmi://localhost:" + getPort() + "/jmxrmi";
//            JMXServiceURL jmxUrl = new JMXServiceURL(url);
//            JMXConnectorServer jmxService = JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mbServer);
//            jmxService.start();
//            JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl, null);
            // jmxConnector.getMBeanServerConnection();
            if (this.processJmx == null) {
                this.processJmx = new ProcessJmx();
            }
            this.mbServerConnection = this.processJmx.getMBeanServerConnection(ProcessUtil.getProcessID());
            this.registerMBeans("WatchDog");
            this.startHtmlAdaptor();
        } catch (Exception e) {
            logger.error("Jmx start method invoke-->{} {}", e.getMessage(), e);
            return false;
        }
        logger.info("============== Jmx started ==============");
        return true;
    }

    public Jmx startChildJmx(int pid, String appName) {
        logger.info("正在进行子进程 pid={} appName={} jmx启动服务", pid, appName);
        Jmx childJmx = null;
        if (pid <= 0) {
            return childJmx;
        }
        String processId = pid + "";
        AbstractManagedObject targetServer = null;
        appName = appName == null ? "" : appName;
        for (AbstractManagedObject server : mbeanList) {
            String sn = server.getName() == null ? "" : server.getName();
            if (appName.equals(sn)) {
                targetServer = server;
                break;
            }
        }
        if (targetServer != null && processId.equals(targetServer.jmx.pid)) {
            return targetServer.jmx;
        }

        if (targetServer != null) {
            childJmx = targetServer.jmx;
            instances.put(childJmx.pid, null);
        } else {
            childJmx = Jmx.getInstance(processId);
        }

        try {
//            JVMReflex jvmReflex = JVMReflex.newInstance();
//            Object vm = jvmReflex.getMethodAttachToVM().invoke(null, processId);
//            if (vm != null) {
//                Properties jvmProperties = (Properties) jvmReflex.getMethodGetAgentProperties().invoke(vm, (Object[]) null);
//                Properties systemProperties = (Properties) jvmReflex.getMethodGetSystemProperties().invoke(vm, (Object[]) null);
//                String localConnectorAddress = "com.sun.management.jmxremote.localConnectorAddress";
//                if (jvmProperties.get(localConnectorAddress) == null) {
//                    String jversion = (String) systemProperties.get("java.version");
//                    if (null != jversion && jversion.startsWith("9.")) {
//                        if (null != jvmReflex.getMethodStartLocalManagementAgent()) {
//                            jvmReflex.getMethodStartLocalManagementAgent().invoke(vm, (Object[]) null);
//                        }
//                    } else {
//                        String agent = systemProperties.getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
//                        jvmReflex.getMethodLoadAgent().invoke(vm, agent, "com.sun.management.jmxremote");
//                    }
//                    jvmProperties = (Properties) jvmReflex.getMethodGetAgentProperties().invoke(vm, (Object[]) null);
//                }
//                jvmReflex.getMethodDetachFromVM().invoke(vm);
//                String address = (String) jvmProperties.get(localConnectorAddress);
//                JMXServiceURL jmxUrl = new JMXServiceURL(address);
//                JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxUrl, checkerEnvMap);
//                childJmx.mbServerConnection = jmxConnector.getMBeanServerConnection();
//                if (targetServer == null) {
//                    mbeanList.add(new WatchDogService(childJmx, appName, true));
//                } else {
//                    if (targetServer instanceof WatchDogService) {
//                        WatchDogService wdService = (WatchDogService) targetServer;
//                        wdService.mbsc = childJmx.getMbServerConnection();
//                    }
//                }
//            }
            if (childJmx.processJmx == null) {
                childJmx.processJmx = new ProcessJmx();
            }
            childJmx.mbServerConnection = childJmx.processJmx.getMBeanServerConnection(pid);
            if (targetServer == null) {
                mbeanList.add(new WatchDogService(childJmx, appName, true));
            } else {
                if (targetServer instanceof WatchDogService) {
                    WatchDogService wdService = (WatchDogService) targetServer;
                    wdService.mbsc = childJmx.getMbServerConnection();
                }
            }
            this.setChildJmx(childJmx);
            logger.info("子进程 pid={} appName={} jmx启动成功", pid, appName);
        } catch (Exception e) {
            logger.error("启动子进程 pid={} appName={} jmx服务异常:{} {}", pid, appName, e.getMessage(), e);
            //TODO 发送告警通知
        }
        return childJmx;
    }

    public boolean startHtmlAdaptor() {
        try {
            ObjectName adapterName = new ObjectName(JmxUtil.DOMAIN + ":name=htmladapter,port=" + getHtmlAdaptorPort());
            HtmlAdaptorServer adapter = new HtmlAdaptorServer(getHtmlAdaptorPort());
            mbServer.registerMBean(adapter, adapterName);
            adapter.start();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void stop() {
        try {
            for (AbstractManagedObject mb : mbeanList) {
                mb.unregisterSelf();
            }
            mbeanList.clear();
            //jmxService.stop();
            Jmx.clearJmx(this.pid);
        } catch (Exception e) {
        }
    }

    public void stopChildJmx(int pid) {
        String processId = pid + "";
        Jmx jmx = instances.get(processId);
        if (jmx != null) {
            jmx.stop();
            this.setChildJmx(null);
        }
        logger.info("子进程（{}）JMX服务停止", pid);
    }

    private void registerMBeans(String serviceName) {
        if (mbServer == null) {
            logger.info("registerMBeans MBSERVER = NULL");
            return;
        }
        logger.info("registerMBeans 准备读取配置文件");
        try {
            String mbeanConfig = PropertiesUtil.getValue("jmx.mbean.define.path");
            if (mbeanConfig == null || "".equals(mbeanConfig)) {
                return;
            }
            String[] mbeanDefinePathAry = mbeanConfig.split(",");
            if (mbeanDefinePathAry.length == 0) {
                return;
            }
            //加载文件夹下所有文件
            for (String mbeanDefinePath : mbeanDefinePathAry) {
                List<String> list = PackageUtil.getClassName(mbeanDefinePath);
                for (String clsName : list) {
                    Class<?> cls = Class.forName(clsName);
                    Constructor cons = cls.getDeclaredConstructor(Jmx.class, String.class, boolean.class);
                    logger.info("registerMBeans 加载class：{}", clsName);
                    mbeanList.add((AbstractManagedObject) cons.newInstance(this, serviceName, false));
                }
            }
        } catch (Exception e) {
            logger.error("Jmx registerMBeans method invoke-->{} {}", e.getMessage(), e);
        }
    }

    public ObjectInstance register(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        logger.info("Jmx register MBean : {}", name.getCanonicalName());
        return mbServer.registerMBean(object, name);
    }

    public void unregister(ObjectName name) throws MBeanRegistrationException, InstanceNotFoundException {
        mbServer.unregisterMBean(name);
    }

    public static ObjectName getObjectName(String domain, Map<String, String> properties) throws MalformedObjectNameException {
        StringBuilder cb = new StringBuilder();
        cb.append(domain);
        cb.append(':');
        boolean isFirst = true;
        String type = (String) properties.get("type");
        if (type != null) {
            cb.append("type=");
            if (escapePattern.matcher(type).find()) {
                type = ObjectName.quote(type);
            }

            cb.append(type);
            isFirst = false;
        }
        Iterator i$ = properties.keySet().iterator();
        while (true) {
            String key;
            do {
                if (!i$.hasNext()) {
                    return new ObjectName(cb.toString());
                }
                key = (String) i$.next();
            } while (key.equals("type"));

            if (!isFirst) {
                cb.append(',');
            }

            isFirst = false;
            cb.append(key);
            cb.append('=');
            String value = (String) properties.get(key);
            if (value.length() == 0 || escapePattern.matcher(value).find() && (!value.startsWith("\"") || !value.endsWith("\""))) {
                value = ObjectName.quote(value);
            }

            cb.append(value);
        }
    }

    public MBeanServerConnection getMbServerConnection() {
        return mbServerConnection;
    }

    public Jmx getChildJmx() {
        return childJmx;
    }

    public void setChildJmx(Jmx childJmx) {
        this.childJmx = childJmx;
    }
}
