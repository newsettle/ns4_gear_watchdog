package com.creditease.ns4.gear.watchdog.common.chatbot.entry;

public class BotMessage {

    private String qqGroupId; //qq群组ID
    private String wxGroupName; //微信群组名称
    private String msgId; //消息唯一标识
    private String type; //wechat qq  wxqq
    private String user; // @的用户
    private String msg; // 消息 文本内容
    private String html; //html格式  将会被转换为图片
    private String remark; //消息来源


    public String getQqGroupId() {
        return qqGroupId;
    }

    public void setQqGroupId(String qqGroupId) {
        this.qqGroupId = qqGroupId;
    }

    public String getWxGroupName() {
        return wxGroupName;
    }

    public void setWxGroupName(String wxGroupName) {
        this.wxGroupName = wxGroupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
