package com.creditease.ns4.gear.watchdog.monitor.es;

import com.creditease.ns.log.NsLog;
import com.creditease.ns4.gear.watchdog.common.PropertiesUtil;
import com.creditease.ns4.gear.watchdog.common.log.NsLogger;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ESConfig {
    private static final NsLog logger = NsLogger.getWatchdogLogger();

    private static TransportClient client;

    static {
        try {

            String host = PropertiesUtil.getValue("elastic.host");
            int port = PropertiesUtil.getInteger("elastic.port");
            String clusterName = PropertiesUtil.getValue("elastic.cluster.name");
            TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(host), port);
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings);
            client.addTransportAddress(transportAddress);
        } catch (UnknownHostException e) {
            logger.error("get es client error", e);
        }
    }

    public static Client client() {
        return client;
    }

    public static void destroy() {
        if (client != null) {
            client.close();
        }
    }

    public static void main(String[] args) {
        client();
    }
}
