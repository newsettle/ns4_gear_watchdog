package com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.serializer;

import com.alibaba.fastjson.JSON;
import com.creditease.ns4.gear.watchdog.common.DateUtil;
import com.creditease.ns4.gear.watchdog.common.IPUtil;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.constant.TaildirSourceConfigurationConstants;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.model.LogEntity;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Event;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author outman
 * @description Event序列化
 * @date 2019/3/7
 */
public class ElasticSearchEventSerializer {

    public static final Charset charset = Charset.defaultCharset();

    /**
     * 日志ip
     */
    public static final String IP = IPUtil.getHostAddress(true, false);

    /**
     * 封装日志归集对象
     *
     * @param event 待归集的日志
     */
    public String serializer(Event event) {
        LogEntity logEntity = new LogEntity();
        Map<String, String> headers = Maps.newHashMap(event.getHeaders());
        String timestamp = headers.get(TaildirSourceConfigurationConstants.TIMESTAMP_HEADER_KEY);
        long timestampMs;
        if (!StringUtils.isBlank(timestamp)
                && StringUtils.isBlank(headers.get("@timestamp"))) {
            timestampMs = Long.parseLong(timestamp);
        } else {
            timestampMs = System.currentTimeMillis();
        }
        logEntity.setTimestamp(DateUtil.timeToStr(timestampMs, DateUtil.DATE_PATTERN_ZONE));
        //真正的日志时间可以从日志中抽取出来
        logEntity.setLogTime(DateUtil.timeToStr(timestampMs, DateUtil.DATE_PATTERN_FULL));
        //设置日志文件名称
        String path = headers.get(TaildirSourceConfigurationConstants.ABSOLUTE_FILE_PATH_HEADER_KEY);
        //日志来源
        logEntity.setSource(path);
        //读取文件的位置
        String offsetStr = headers.get(TaildirSourceConfigurationConstants.OFFSET_HEADER_KEY);
        long offset;
        try {
            offset = Long.parseLong(offsetStr);
        } catch (Exception e) {
            offset = -99;
        }
        logEntity.setOffset(offset);
        //读取文件的行号
        String lineNumStr = headers.get(TaildirSourceConfigurationConstants.LINE_NUM_HEADERY_KEY);
        long lineNum;
        try {
            lineNum = Long.parseLong(lineNumStr);
        } catch (Exception e) {
            lineNum = -99;
        }
        logEntity.setLineNum(lineNum);
        //为了迎合日志查询组件而设计
        String host = headers.get(TaildirSourceConfigurationConstants.HOST_HEADER_KEY);
        if (StringUtils.isBlank(host)) {
            host = IP;
        }
        Map<String, String> beat = new HashMap<>();
        beat.put("hostname", host);
        logEntity.setBeat(beat);
        //设置日志信息
        String body = new String(event.getBody(), charset);
        logEntity.setMessage(body);
        return JSON.toJSONString(logEntity);
    }
}
