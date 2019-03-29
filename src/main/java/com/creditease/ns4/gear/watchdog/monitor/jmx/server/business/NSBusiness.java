package com.creditease.ns4.gear.watchdog.monitor.jmx.server.business;

import com.alibaba.fastjson.JSONObject;
import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.JmxUtil;
import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.monitor.MessageType;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataMXBean;
import com.creditease.ns4.gear.watchdog.monitor.es.ESUtil;
import com.creditease.ns4.gear.watchdog.monitor.jmx.AbstractManagedObject;
import com.creditease.ns4.gear.watchdog.monitor.jmx.Jmx;
import com.creditease.ns4.gear.watchdog.monitor.jmx.NSBusinessMXBean;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description ns business
 * @Author karma
 * @Date 2019/1/23 下午2:59
 * @Version 1.0
 **/
public class NSBusiness extends AbstractManagedObject implements NSBusinessMXBean {

    private static final NsLog logger = NsLogger.getWatchdogLogger();

    public NSBusiness(Jmx jmx, String name, boolean isChildProcessMXBean) {
        super(jmx, name, isChildProcessMXBean);
    }


    @Override
    public String getNsSendQueueBuffer() {
        try {
            MonitorDataMXBean proxy = getMonitorDataMXBean();
            return proxy.getData(MessageType.NS_SEND_QUEUE_BUFFER.name());
        } catch (Exception e) {
            logger.error("获取发送队列异常：{} {}", e.getMessage(), e);
        }
        return "NONE";
    }

    @Override
    public String getNsReceiveQueueBuffer() {
        try {
            MonitorDataMXBean proxy = getMonitorDataMXBean();
            return proxy.getData(MessageType.NS_RECEIVE_QUEUE_BUFFER.name());
        } catch (Exception e) {
            logger.error("获取接收队列异常：{} {}", e.getMessage(), e);
        }
        return "NONE";
    }

    @Override
    public String getNsTradeMessageBuffer() {
        try {
            MonitorDataMXBean proxy = getMonitorDataMXBean();
            String nstradeMessage = proxy.getData(MessageType.NS_TRADE_MESSAGE.name());
            return nstradeMessage;
        } catch (Exception e) {
            logger.error("获取接收队列异常：{} {}", e.getMessage(), e);
        }
        return "NONE";
    }


    public static void saveTradeDataToEs() {
        try {
            MBeanServerConnection mbsc = Jmx.getInstance().getChildJmx().getMbServerConnection();
            MonitorDataMXBean proxy = JmxUtil.getMonitorDataMXBean(mbsc);
            String nstradeMessage = proxy.getData(MessageType.NS_TRADE_MESSAGE.name());
            logger.info("获取业务数据{} ", nstradeMessage);
            if (null != nstradeMessage && !"".equals(nstradeMessage)) {
                logger.info("保存业务数据{} ", nstradeMessage);
                ESUtil util = new ESUtil();
                Map<String, List<Object>> strtomap = (Map<String, List<Object>>) JSONObject.parse(nstradeMessage);
                if (strtomap != null && strtomap.size() != 0) {
                    logger.info("循环保存业务数据{} ", nstradeMessage);
                    Iterator<String> iterator = strtomap.keySet().iterator();
                    while (iterator.hasNext()) {
                        String msgId = iterator.next();
                        List<Object> msgList = strtomap.get(msgId);
                        for (Object event : msgList) {
                            Map<String, Object> msgMap = new HashMap<String, Object>();
                            msgMap.put("msgId", msgId);
                            msgMap.put("content", event);
                            util.addDocument(PropertiesUtil.getValue("elastic.esindices"), PropertiesUtil.getValue("elastic.estype"), msgMap);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取接收队列异常：{} {}", e.getMessage(), e);
        }
    }

    private MonitorDataMXBean getMonitorDataMXBean() throws MalformedObjectNameException {
        MBeanServerConnection mbsc = Jmx.getInstance().getChildJmx().getMbServerConnection();
        return JmxUtil.getMonitorDataMXBean(mbsc);
    }
}
