package com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.builder;

import com.creditease.ns4.gear.watchdog.monitor.collect.log.constant.ElasticSearchHighSinkConstants;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.conf.ComponentConfiguration;
import org.apache.flume.formatter.output.BucketPath;

/**
 * @author outman
 * @description es索引名称不按照时间进行分割的Builder
 * @date 2019/3/6
 */
public class SimpleIndexNameBuilder implements IndexNameBuilder {

    private String indexName;

    @Override
    public String getIndexName(Event event) {
        return BucketPath.escapeString(indexName, event.getHeaders());
    }

    @Override
    public String getIndexSuffix(Event event) {
        return null;
    }

    @Override
    public String getIndexPrefix(Event event) {
        return BucketPath.escapeString(indexName, event.getHeaders());
    }

    @Override
    public void configure(Context context) {
        indexName = context.getString(ElasticSearchHighSinkConstants.INDEX_NAME);
    }

    @Override
    public void configure(ComponentConfiguration conf) {
    }
}
