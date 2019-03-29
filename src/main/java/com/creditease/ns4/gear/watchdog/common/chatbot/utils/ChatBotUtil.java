package com.creditease.ns4.gear.watchdog.common.chatbot.utils;

import com.alibaba.fastjson.JSONObject;
import com.creditease.ns4.gear.watchdog.common.chatbot.entry.BotMessage;

import org.apache.commons.lang.StringUtils;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class ChatBotUtil {

    public static String URL = "";

    public static void init() {
        Properties prop = new Properties();
        InputStream in = null;
        try {
            ClassLoader classLoader = ChatBotUtil.class.getClassLoader();// 读取属性文件xxxxx.properties
            in = classLoader.getResourceAsStream("config/chatbot.properties");
            prop.load(in); /// 加载属性列表
            String url = prop.getProperty("chatbot.url");
            if (! StringUtils.isEmpty(url)) {
                URL = url;
            }
            System.out.println(url);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            }catch (Exception e) {
            }
        }

    }

    /**
     * 发送消息
     * @param message 消息内容
     * @return
     */
    public static String send(BotMessage message) {
        if (URL.equals("")) {
            init();
        }

        String  valid = validMessage(message);
        if (!StringUtils.isEmpty(valid)) {
            return valid;
        }

        HashMap<String,String> param = convertHashMap(message);

        return JSONObject.toJSONString(HttpClientUtil.doUmpHttp_HttpClient(param , URL));

    }

    private static HashMap<String,String> convertHashMap(BotMessage message) {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("qqGroupId", message.getQqGroupId());
        map.put("wxGroupName", message.getWxGroupName());
        map.put("msgId", message.getMsgId());
        map.put("type", message.getType());
        map.put("user", message.getUser());
        map.put("msg", message.getMsg());
        map.put("html", message.getHtml());
        map.put("remark", message.getRemark());
        return map;
    }

    private static String validMessage(BotMessage message) {
        HashMap<String,String> resultMap = new HashMap<String,String>();
        String result = "";
        String retCode = ChatConstants.MSG_SUCCESS;

        if (StringUtils.isEmpty(message.getMsgId())) {
            retCode = ChatConstants.MSG_FAIL;
            result = "msgId 不能为空！";
        }

        String type = message.getType();

        if (StringUtils.isEmpty(type)) {
            retCode = ChatConstants.MSG_FAIL;
            result = "type 不能为空！";
        }

        if (type.equals(ChatConstants.CHATBOT_QQ)) {
            if (StringUtils.isEmpty(message.getQqGroupId())) {
                retCode = ChatConstants.MSG_FAIL;
                result = "qqGroupId 不能为空！";
            }
        } else if (type.equals(ChatConstants.CHATBOT_WX)) {
            if (StringUtils.isEmpty(message.getWxGroupName())) {
                retCode = ChatConstants.MSG_FAIL;
                result = "wxGroupName 不能为空！";
            }
        } else {
            if (StringUtils.isEmpty(message.getWxGroupName()) || StringUtils.isEmpty(message.getQqGroupId())) {
                retCode = ChatConstants.MSG_FAIL;
                result = "wxGroupName 和 qqGroupId 不能为空！";
            }
        }

        if (StringUtils.isEmpty(URL)) {
            result = "请初始化URL！";
            retCode = ChatConstants.MSG_FAIL;
        }



        if (!result.equals("")) {
            if (type.equals(ChatConstants.CHATBOT_QQ)) {
                result = "【QQ消息发送失败】" + result;
            } else {
                result = "【微信消息发送失败】" + result;
            }
        }

        resultMap.put("code",retCode);
        resultMap.put("message",result);

        if (retCode.equals(ChatConstants.MSG_FAIL)) {
            return JSONObject.toJSONString(resultMap);
        }
        return "";
    }



}
