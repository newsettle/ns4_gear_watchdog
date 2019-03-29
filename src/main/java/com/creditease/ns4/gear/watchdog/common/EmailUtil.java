/**
 * CreditEase.cn Inc.
 * Copyright (c) 2006-2016 All Rights Reserved.
 */
package com.creditease.ns4.gear.watchdog.common;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.factory.ThreadPoolManager;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.*;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author yaqiangzhao
 * @version v1.0 创建时间：2019-01-25 下午13:48:09
 * 邮件发送工具类
 *
 */
public class EmailUtil {
    private static final NsLog logger = NsLogger.getWatchdogLogger();

    /**
     * 初始化连接邮件服务器的会话信息
     **/
    private static Properties PROPS = null;
    /**
     * 异步发送邮件线程池
     */
    private static ExecutorService namedAsynSendEmailFactoryPool = null;

    /**
     * 初试化邮箱配置信息
     */
    static {
        PROPS = new Properties();
        PROPS.setProperty("mail.transport.protocol", PropertiesUtil.getValue("mail.protocol"));
        PROPS.setProperty("mail.smtp.host", PropertiesUtil.getValue("mail.host"));
        PROPS.setProperty("mail.smtp.port", PropertiesUtil.getValue("mail.port"));
        PROPS.setProperty("mail.smtp.auth", PropertiesUtil.getValue("mail.auth"));
        // 设置是否使用ssl安全连接 ---一般都使用
        PROPS.setProperty("mail.smtp.ssl.enable", PropertiesUtil.getValue("mail.ssl.enable"));
        PROPS.setProperty("mail.debug", PropertiesUtil.getValue("mail.debug"));
        try {
            namedAsynSendEmailFactoryPool = ThreadPoolManager.getInstance().newExecutorService("send-email-pool-%d", 1, 2, 50L, TimeUnit.MILLISECONDS, 10);
        } catch (Exception e) {
            logger.error("创建异步发送邮件线程池错误：{}", e.getMessage());
        }
    }

    /**
     * 发送邮件
     */
    public static void send(String title, String content) {
        send(title, content, true);
    }

    /**
     * 发送邮件
     */
    public static void send(String title, String content, boolean isAsyn) {
        asynSend(PropertiesUtil.getValue("mail.receiver"), title, content, isAsyn);
    }

    /**
     * 异步发送邮件
     *
     * @param email   邮箱
     * @param title   标题
     * @param content 内容
     */
    private static void asynSend(final String email, final String title, final String content, boolean isAsyn) {
        if (isAsyn) {
            if (namedAsynSendEmailFactoryPool.isShutdown()) {
                return;
            }
            namedAsynSendEmailFactoryPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        sendTextEmail(email, title, content, null);
                        logger.info("发送邮件{}成功!", email);
                    } catch (Exception e) {
                        logger.error("发送 {} 邮件给{} 失败{}", content, email, e.getMessage());
                    }
                }
            });
        } else {
            try {
                sendTextEmail(email, title, content, null);
                logger.info("发送邮件{}成功!", email);
            } catch (Exception e) {
                logger.error("发送 {} 邮件给{} 失败{}", content, email, e.getMessage());
            }
        }
    }


    /**
     * @param email      收件人地址
     * @param title      标题
     * @param content    正文《文本》
     * @param attachment 附件《file》
     * @throws Exception
     * @throws MessagingException
     */
    private static void sendTextEmail(String email, String title, String content, File attachment) throws Exception {
        // 创建Session实力对象
        Session session = Session.getDefaultInstance(PROPS);
        // 创建MimeMessage实例对象
        MimeMessage message = new MimeMessage(session);
        // 设置发件人
        message.setFrom(new InternetAddress(PropertiesUtil.getValue("mail.sender.name")));
        // 设置邮件主题
        message.setSubject(title);
        // 设置收件人
        message.setRecipients(RecipientType.TO, email);
        // 设施发送时间
        message.setSentDate(new Date());

        // 向multipart对象中添加邮件的各个部分内容，包括文本内容和附件

        Multipart multipart = new MimeMultipart();
        // 添加邮件正文
        BodyPart contentPart = new MimeBodyPart();
        contentPart.setContent(content, "text/html;charset=UTF-8");
        multipart.addBodyPart(contentPart);

        // 添加附件的内容
        if (attachment != null) {
            BodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            /** 网上流传的解决文件名乱码的方法，其实用MimeUtility.encodeWord就可以很方便的搞定
             * 这里很重要，通过下面的Base64编码的转换可以保证你的中文附件标题名在发送时不会变成乱码
             * sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
             * messageBodyPart.setFileName("=?GBK?B?" + enc.encode(attachment.getName().getBytes()) + "?=");
             * MimeUtility.encodeWord可以避免文件名乱码
             **/
            attachmentBodyPart.setFileName(MimeUtility.encodeWord(attachment.getName()));
            multipart.addBodyPart(attachmentBodyPart);
        }

        message.setContent(multipart);
        // 保存并生成最终的邮件内容
        message.saveChanges();

        // 获得Transport实例对象发送邮件
        Transport transport = session.getTransport();
        // 打开连接，设置发送邮箱的账号和密码
        transport.connect(PropertiesUtil.getValue("mail.sender.name"), PropertiesUtil.getValue("mail.sender.pwd"));
        // 讲message对象 传递给transport对象，将邮件发送出去
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
    }

    public static void shutdown() {
        logger.info("停止异步发送邮件线程池");
        namedAsynSendEmailFactoryPool.shutdown();
    }

    public static void main(String[] args) {
        EmailUtil.send("test","test");
    }
}