package com.creditease.ns4.gear.watchdog.common.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.framework.ext.plugin.MonitorEvent;
import com.creditease.framework.ext.plugin.NSEnvironment;
import com.creditease.ns.log.NsLog;
import com.creditease.ns.transporter.buffer.DefaultBufferManagerProxy;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import java.util.*;

/**
 * @author outman
 * @description 监控数据缓存
 * @date 2019/1/15
 */
public class MonitorDataFrame {

    private static MonitorDataFrame monitorDataFrame = new MonitorDataFrame();
    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();
    private Map<MessageType, LimitQueue<Object>> monitorDataMap = new HashMap<>();

    private MonitorDataFrame() {
        monitorDataMap = new HashMap<MessageType, LimitQueue<Object>>();
        monitorDataMap.put(MessageType.NS_RECEIVE_QUEUE_BUFFER, new LimitQueue<Object>(1));
        monitorDataMap.put(MessageType.NS_SEND_QUEUE_BUFFER, new LimitQueue<Object>(1));
        monitorDataMap.put(MessageType.NS_TRADE_MESSAGE, new LimitQueue<Object>(1));
    }

    public static MonitorDataFrame getInstance() {
        return monitorDataFrame;
    }

    public void putData(MessageType messageType, Object obj) {
        monitorDataMap.get(messageType).offer(obj);
    }

    public Object getData(MessageType messageType) {
        //TODO 转成json
        if(null != monitorDataMap.get(messageType) && monitorDataMap.get(messageType).size()>0){
            NsLog.getFramLog().info("收到监控消息 {}", JSON.toJSONString(monitorDataMap.get(messageType)));
            return monitorDataMap.get(messageType).getFirst();
        }else {
            return null;
        }
    }

    public Object pollData(MessageType messageType) {
        //TODO 转成json
        if(null != monitorDataMap.get(messageType) && monitorDataMap.get(messageType).size()>0){
            NsLog.getFramLog().info("收到监控消息 {}", JSON.toJSONString(monitorDataMap.get(messageType)));
            return monitorDataMap.get(messageType).poll();
        }else {
            return null;
        }
    }


    public Object getData(MessageType messageType, NSEnvironment nsEnvironment) {
        Object message = null;
        if(MessageType.NS_RECEIVE_QUEUE_BUFFER.equals(messageType)) {
            DefaultBufferManagerProxy proxy = nsEnvironment.getEnvironmentInfo();
            Set<String> allQueueNamesForReceiving = proxy.getAllQueueNamesForReceiving();
            if(allQueueNamesForReceiving != null && allQueueNamesForReceiving.size() !=0) {
                Iterator<String> iterator = allQueueNamesForReceiving.iterator();
                String queueName;
                long queueSize;
                StringBuilder builder = new StringBuilder("[");
                while(iterator.hasNext()) {
                    queueName = iterator.next();
                    queueSize = proxy.sizeOfReceiveBufferOf(queueName);
                    builder.append(queueName).append(":").append(queueSize).append(" ");
                }
                builder.append("]");
                message = builder.toString();
            }
        }else if(MessageType.NS_SEND_QUEUE_BUFFER.equals(messageType)) {
            DefaultBufferManagerProxy proxy = nsEnvironment.getEnvironmentInfo();
            Set<String> allQueueNamesForSending = proxy.getAllQueueNamesForSending();
            if(allQueueNamesForSending != null && allQueueNamesForSending.size() !=0) {
                Iterator<String> iterator = allQueueNamesForSending.iterator();
                String queueName;
                long queueSize;
                StringBuilder builder = new StringBuilder("[");
                while(iterator.hasNext()) {
                    queueName = iterator.next();
                    queueSize = proxy.sizeOfSendBufferOf(queueName);
                    builder.append(queueName).append(":").append(queueSize).append(" ");
                }
                builder.append("]");
                message = builder.toString();
            }
        }else if(MessageType.NS_TRADE_MESSAGE.equals(messageType)) {
            Map<String,List<MonitorEvent>> tradeMessageMap = (Map<String, List<MonitorEvent>>) pollData(messageType);
            message = JSONObject.toJSONString(tradeMessageMap);
        }
        return message;
    }

}
