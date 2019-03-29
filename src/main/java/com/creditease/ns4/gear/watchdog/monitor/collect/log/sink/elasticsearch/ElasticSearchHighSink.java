package com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.constant.ElasticSearchHighSinkConstants;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.builder.IndexNameBuilder;
import com.creditease.ns4.gear.watchdog.monitor.collect.log.sink.elasticsearch.serializer.ElasticSearchEventSerializer;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static com.creditease.ns4.gear.watchdog.monitor.collect.log.constant.ElasticSearchHighSinkConstants.*;

/**
 * @author outman
 * @description 高版本的es sink version 6.x
 * @date 2019/3/5
 */
public class ElasticSearchHighSink extends AbstractSink implements Configurable {

    private static final NsLog logger = NsLogger.getWatchdogCollectLogger();

    private static final Charset charset = Charset.defaultCharset();

    private final CounterGroup counterGroup = new CounterGroup();

    private String[] serverAddresses = null;

    private String serverUser = null;

    private String serverPassword = null;

    private static final int defaultBatchSize = 100;

    private int batchSize = defaultBatchSize;

    private String indexName = ElasticSearchHighSinkConstants.DEFAULT_INDEX_NAME;

    private String indexType = DEFAULT_INDEX_TYPE;

    private IndexNameBuilder indexNameBuilder;

    private ElasticSearchEventSerializer eventSerializer;

    private SinkCounter sinkCounter;

    private RestHighLevelClient client;


    //执行event的具体操作
    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.READY;
        Channel channel = getChannel();
        Transaction txn = channel.getTransaction();
        List<Event> events = Lists.newArrayList();
        try {
            txn.begin();
            int count;
            for (count = 0; count < batchSize; ++count) {
                //从channel中获取元素,实际是event = queue.poll();如果为空则会抛出异常,会被捕获处理返回null
                Event event = channel.take();
                if (event == null) {
                    break;
                }
                //进行批处理到ES
                events.add(event);
            }

            //当达到设置的批次后进行提交
            if (count <= 0) {
                sinkCounter.incrementBatchEmptyCount();
                counterGroup.incrementAndGet("channel.underflow");
                status = Status.BACKOFF;
            } else {
                if (count < batchSize) {
                    sinkCounter.incrementBatchUnderflowCount();
                    status = Status.BACKOFF;
                } else {
                    sinkCounter.incrementBatchCompleteCount();
                }
                sinkCounter.addToEventDrainAttemptCount(count);
                //提交当前批次到ES
                bulkExecute(events);
            }
            txn.commit();
            sinkCounter.addToEventDrainSuccessCount(count);
            counterGroup.incrementAndGet("transaction.success");
        } catch (Throwable ex) {
            try {
                txn.rollback();
                counterGroup.incrementAndGet("transaction.rollback");
            } catch (Exception ex2) {
                logger.error(
                        "Exception in rollback. Rollback might not have been successful.",
                        ex2);
            }

            if (ex instanceof Error || ex instanceof RuntimeException) {
                logger.error("Failed to commit transaction. Transaction rolled back.",
                        ex);
                Throwables.propagate(ex);
            } else {
                logger.error("Failed to commit transaction. Transaction rolled back.",
                        ex);
                throw new EventDeliveryException(
                        "Failed to commit transaction. Transaction rolled back.", ex);
            }
        } finally {
            txn.close();
        }
        return status;
    }

    //初始化执行获取配置信息
    @Override
    public void configure(Context context) {
        //获取配置信息
        if (StringUtils.isNotBlank(context.getString(ElasticSearchHighSinkConstants.HOST_NAMES))) {
            serverAddresses = StringUtils.deleteWhitespace(
                    context.getString(ElasticSearchHighSinkConstants.HOST_NAMES)).split(",");
        }
        Preconditions.checkState(serverAddresses != null
                && serverAddresses.length > 0, "Missing Param:" + ElasticSearchHighSinkConstants.HOST_NAMES);


        if (StringUtils.isNotBlank(context.getString(ElasticSearchHighSinkConstants.HOST_USER))) {
            this.serverUser = context.getString(ElasticSearchHighSinkConstants.HOST_USER);
        }

        if (StringUtils.isNotBlank(context.getString(ElasticSearchHighSinkConstants.HOST_PASSWORD))) {
            this.serverPassword = context.getString(ElasticSearchHighSinkConstants.HOST_PASSWORD);
        }

        if (StringUtils.isNotBlank(context.getString(INDEX_NAME))) {
            this.indexName = context.getString(INDEX_NAME);
        }

        if (StringUtils.isNotBlank(context.getString(INDEX_TYPE))) {
            this.indexType = context.getString(INDEX_TYPE);
        }

        if (StringUtils.isNotBlank(context.getString(BATCH_SIZE))) {
            this.batchSize = Integer.parseInt(context.getString(BATCH_SIZE));
        }

        logger.info("获取到ES配置信息,address:" + StringUtils.join(serverAddresses) + ",index:" + indexName + ",batchSize:" + batchSize);

        if (sinkCounter == null) {
            sinkCounter = new SinkCounter(getName());
        }

        String indexNameBuilderClass = DEFAULT_INDEX_NAME_BUILDER_CLASS;
        if (StringUtils.isNotBlank(context.getString(INDEX_NAME_BUILDER))) {
            indexNameBuilderClass = context.getString(INDEX_NAME_BUILDER);
        }

        Context indexnameBuilderContext = new Context();
        try {
            @SuppressWarnings("unchecked")
            Class<? extends IndexNameBuilder> clazz
                    = (Class<? extends IndexNameBuilder>) Class
                    .forName(indexNameBuilderClass);
            indexNameBuilder = clazz.newInstance();
            indexnameBuilderContext.put(INDEX_NAME, indexName);
            indexNameBuilder.configure(indexnameBuilderContext);
        } catch (Exception e) {
            logger.error("Could not instantiate index name builder.", e);
            Throwables.propagate(e);
        }

        eventSerializer = new ElasticSearchEventSerializer();

        Preconditions.checkState(StringUtils.isNotBlank(indexName),
                "Missing Param:" + INDEX_NAME);
        Preconditions.checkState(batchSize >= 1, BATCH_SIZE
                + " must be greater than 0");
    }


    //启动sink的一些配置
    @Override
    public synchronized void start() {
        logger.info("start elasticsearch sink......");
        HttpHost[] httpHosts = new HttpHost[serverAddresses.length];
        for (int i = 0; i < serverAddresses.length; i++) {
            String[] hostPort = serverAddresses[i].trim().split(":");
            String host = hostPort[0].trim();
            int port = hostPort.length == 2 ? Integer.parseInt(hostPort[1].trim())
                    : DEFAULT_PORT;
            logger.info("elasticsearch host:{},port:{}", host, port);
            httpHosts[i] = new HttpHost(host, port, "http");
        }

        RestClientBuilder builder = RestClient.builder(httpHosts);
        if (StringUtils.isNotBlank(serverUser) || StringUtils.isNotBlank(serverPassword)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(serverUser, serverPassword));
            builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(
                        HttpAsyncClientBuilder httpClientBuilder) {
                    return httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }
        client = new RestHighLevelClient(builder);
        sinkCounter.start();
        super.start();
    }

    //关闭资源
    @Override
    public synchronized void stop() {
        logger.info("stop elasticsearch sink......");
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sinkCounter.stop();
        super.stop();
    }


    //ES批处理的方法
    public void bulkExecute(List<Event> events) throws Exception {
        //批量插入数据
        BulkRequest request = new BulkRequest();
        String indexName = null;
        for (Event event : events) {
            //如果没有切换天，那么索引可以服用，无需重复创建
            if (StringUtils.isEmpty(indexName) || !indexName.endsWith(indexNameBuilder.getIndexSuffix(event))) {
                indexName = indexNameBuilder.getIndexName(event);
            }
            request.add(new IndexRequest(indexName, indexType).source(eventSerializer.serializer(event), XContentType.JSON));
        }
        BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
        TimeValue took = bulkResponse.getTook();
        logger.debug("[批量新增花费的毫秒]:" + took + "," + took.getMillis() + "," + took.getSeconds() + ",events[" + events.size() + "]");
    }
}
