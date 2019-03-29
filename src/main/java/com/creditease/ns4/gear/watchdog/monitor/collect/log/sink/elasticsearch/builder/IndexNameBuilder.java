package com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.builder;

import org.apache.flume.Event;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurableComponent;

/**
 * @author outman
 * @description es索引名称Builder
 * @date 2019/3/6
 */
public interface IndexNameBuilder extends Configurable,
        ConfigurableComponent {
    /**
     * Gets the name of the index to use for an index request
     *
     * @param event Event which determines index name
     * @return index name of the form 'indexPrefix-indexDynamicName'
     */
    public String getIndexName(Event event);

    /**
     * Gets the name of the index to use for an index request
     *
     * @param event Event which determines index name
     * @return index name of the form 'indexPrefix-indexDynamicName'
     */
    public String getIndexSuffix(Event event);

    /**
     * Gets the prefix of index to use for an index request.
     *
     * @param event Event which determines index name
     * @return Index prefix name
     */
    public String getIndexPrefix(Event event);
}
