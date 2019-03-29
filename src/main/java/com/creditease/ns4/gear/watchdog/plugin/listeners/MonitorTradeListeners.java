package com.creditease.ns4.gear.watchdog.plugin.listeners;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.creditease.framework.ext.plugin.MonitorEvent;
import com.creditease.framework.ext.plugin.MonitorListener;
import com.creditease.framework.pojo.ServiceMessage;
import com.creditease.ns.log.NsLog;
import com.creditease.ns.mq.model.Message;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.common.monitor.MessageType;
import com.creditease.ns4.gear.watchdog.common.monitor.MonitorDataFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorTradeListeners implements MonitorListener {
    public static Map<String,List<MonitorEvent>> tradeMessageMap;
    private static final NsLog logger = NsLogger.getWatchdogPluginLogger();
    @Override
    public void processMonitorEvent(MonitorEvent monitorEvent) {
        MonitorDataFrame dataFrame = MonitorDataFrame.getInstance();

        try{
            if (null != dataFrame.getData(MessageType.NS_TRADE_MESSAGE) ){
                tradeMessageMap = (Map<String, List<MonitorEvent>>) dataFrame.getData(MessageType.NS_TRADE_MESSAGE);
            }else {
                tradeMessageMap = new HashMap<String,List<MonitorEvent>>(100);
                dataFrame.putData(MessageType.NS_TRADE_MESSAGE,tradeMessageMap);
            }
            NsLog.getFramLog().info("收到监控消息 {}", JSON.toJSONString(monitorEvent));
            Message message = monitorEvent.getMessage();
            ServiceMessage serviceMessage = monitorEvent.getServiceMessage();
            Object object = (message == null)? serviceMessage:message;
            String traMessage = JSONObject.toJSONString(monitorEvent);
            String msgId = (message == null)? serviceMessage.getHeader().getMessageID():message.getHeader().getMessageID();
            if(tradeMessageMap.containsKey(msgId)){
                List<MonitorEvent> list = tradeMessageMap.get(msgId);
                list.add(monitorEvent);
            }else {
                List<MonitorEvent> list = new ArrayList<MonitorEvent>();
                list.add(monitorEvent);
                tradeMessageMap.put(msgId,list);
            }
            NsLog.getFramLog().info("收到监控消息size {}", JSON.toJSONString(tradeMessageMap));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
