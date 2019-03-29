package com.creditease.ns4.gear.watchdog.monitor.collect.log.constant;

/**
 * @author outman
 * @description es sink所需常量定义
 * @date 2019/3/5
 */
public class ElasticSearchHighSinkConstants {

    /**
     * Comma separated list of hostname:port, if the port is not present the
     * default port '9200' will be used</p>
     * Example:
     * <pre>
     *  127.0.0.1:92001,127.0.0.2:9300
     * </pre>
     */
    public static final String HOST_NAMES = "hostNames";

    /**
     * 用户名
     */
    public static final String HOST_USER = "user";

    /**
     * 密码
     */
    public static final String HOST_PASSWORD = "password";

    /**
     * The name to index the document to, defaults to 'flume'</p>
     * The current date in the format 'yyyy-MM-dd' will be appended to this name,
     * for example 'foo' will result in a daily index of 'foo-yyyy-MM-dd'
     */
    public static final String INDEX_NAME = "indexName";

    /**
     * The type to index the document to, defaults to 'log'
     */
    public static final String INDEX_TYPE = "indexType";

    /**
     * Maximum number of events the sink should take from the channel per
     * transaction, if available. Defaults to 100
     */
    public static final String BATCH_SIZE = "batchSize";

    public static final String DEFAULT_INDEX_NAME = "flume";

    public static final String DEFAULT_INDEX_TYPE = "doc";

    public static final Integer DEFAULT_PORT = 9200;

    /**
     * The fully qualified class name of the index name builder the sink
     * should use to determine name of index where the event should be sent.
     */
    public static final String INDEX_NAME_BUILDER = "indexNameBuilder";

    /**
     * 默认索引名称生成的Builder
     */
    public static final String DEFAULT_INDEX_NAME_BUILDER_CLASS =
            "com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.builder.TimeBasedIndexNameBuilder";
}
