package com.creditease.ns4.gear.watchdog.jmx;

import sun.management.ConnectorAddressLink;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class JMXClient {
    public static void main(String[] args) throws Exception {

        //connect JMX
        //JMXService 根据pid获取 getLocalStubServiceURLFromPID
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
        ObjectName mbeanName = new ObjectName("com.creditease:name=hello");

        MBeanInfo mb = mbsc.getMBeanInfo(new ObjectName("java.lang:type=ClassLoading"));
        for (MBeanAttributeInfo i : mb.getAttributes()) {
            System.out.println(i.getName());
        }

        for (MBeanOperationInfo i : mb.getOperations()) {
            System.out.println(i.getName());
        }

        //print domains
        System.out.println("Domains:---------------");
        String domains[] = mbsc.getDomains();
        for (int i = 0; i < domains.length; i++) {
            System.out.println("Domain[" + i + "] = " + domains[i]);
        }
        System.out.println();

        //MBean count
        System.out.println("MBean count:---------------");
        System.out.println("MBean count = " + mbsc.getMBeanCount());
        System.out.println();


        Set MBeanset = mbsc.queryMBeans(null, null);
        System.out.println("MBeanset.size() : " + MBeanset.size());
        Iterator MBeansetIterator = MBeanset.iterator();
        while (MBeansetIterator.hasNext()) {
            ObjectInstance objectInstance = (ObjectInstance) MBeansetIterator
                    .next();
            ObjectName objectName = objectInstance.getObjectName();
            String canonicalName = objectName.getCanonicalName();
            System.out.println("canonicalName : " + canonicalName);

            if (canonicalName
                    .equals("Catalina:host=localhost,type=Cluster")) {
                // Get details of cluster MBeans
                System.out.println("Cluster MBeans Details:");
                System.out
                        .println("=========================================");
                // getMBeansDetails(canonicalName);
                String canonicalKeyPropList = objectName
                        .getCanonicalKeyPropertyListString();
            }
        }


        //process attribute
        System.out.println("process attribute:---------------");
        mbsc.setAttribute(mbeanName, new Attribute("Name", "newName")); //set value
        System.out.println("Name = " + mbsc.getAttribute(mbeanName, "Name")); //get value
        System.out.println();

        //invoke via proxy
        System.out.println("invoke via proxy:---------------");
        HelloMBean proxy = (HelloMBean) MBeanServerInvocationHandler.newProxyInstance(mbsc, mbeanName, HelloMBean.class, false);
        System.out.println(proxy.getName());
        proxy.helloWorld("zhangsan");
        System.out.println();

        //invoke via rmi
        System.out.println("invoke via rmi:---------------");
        System.out.println(mbsc.invoke(mbeanName, "printHello", null, null));
        System.out.println(mbsc.invoke(mbeanName, "printHello", new Object[]{"lisi"}, new String[]{String.class.getName()}));
        System.out.println();

        //get mbean information
        System.out.println("get mbean information:---------------");
        MBeanInfo info = mbsc.getMBeanInfo(mbeanName);
        System.out.println("Hello Class:" + info.getClassName());
        System.out.println("Hello Attribute:" + info.getAttributes()[0].getName());
        System.out.println("Hello Operation:" + info.getOperations()[0].getName());
        System.out.println();

        //ObjectName of MBean
        System.out.println("ObjectName of MBean:---------------");
        Set set = mbsc.queryMBeans(null, null);
        for (Iterator it = set.iterator(); it.hasNext(); ) {
            ObjectInstance oi = (ObjectInstance) it.next();
            String canonicalName = oi.getObjectName().getCanonicalName();
            System.out.println("objectName : " + oi.getObjectName() + "              canonicalName : " + canonicalName);
        }


        System.out.println("其他信息:---------------");
        ObjectName runtimeObjName = new ObjectName("java.lang:type=Runtime");
        System.out.println("厂商:"
                + (String) mbsc.getAttribute(runtimeObjName, "VmVendor"));
        System.out.println("程序:"
                + (String) mbsc.getAttribute(runtimeObjName, "VmName"));
        System.out.println("版本:"
                + (String) mbsc.getAttribute(runtimeObjName, "VmVersion"));
        Date starttime = new Date((Long) mbsc.getAttribute(runtimeObjName,
                "StartTime"));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("启动时间:" + df.format(starttime));

        Long timespan = (Long) mbsc.getAttribute(runtimeObjName, "Uptime");
        System.out.println("连续工作时间:" + JMXTest.formatTimeSpan(timespan));


        // 堆使用率
        ObjectName heapObjName = new ObjectName("java.lang:type=Memory");
        MemoryUsage heapMemoryUsage = MemoryUsage
                .from((CompositeDataSupport) mbsc.getAttribute(heapObjName,
                        "HeapMemoryUsage"));
        long maxMemory = heapMemoryUsage.getMax();// 堆最大
        long commitMemory = heapMemoryUsage.getCommitted();// 堆当前分配
        long usedMemory = heapMemoryUsage.getUsed();
        System.out.println("heap:" + (double) usedMemory * 100
                / commitMemory + "%");// 堆使用率

        MemoryUsage nonheapMemoryUsage = MemoryUsage
                .from((CompositeDataSupport) mbsc.getAttribute(heapObjName,
                        "NonHeapMemoryUsage"));
        long noncommitMemory = nonheapMemoryUsage.getCommitted();
        long nonusedMemory = heapMemoryUsage.getUsed();
        System.out.println("nonheap:" + (double) nonusedMemory * 100
                / noncommitMemory + "%");

        jmxc.close();
    }

    private static JMXServiceURL getLocalStubServiceURLFromPID(int pid)
            throws IOException {
        String address = ConnectorAddressLink.importFrom(pid);
        if (address != null) {
            return new JMXServiceURL(address);
        }
        return null;

    }
}