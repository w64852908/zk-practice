package com.lanxiang.zk.practice.service.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class ZkConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkConnection.class);

    private CuratorFramework curator;

    private String connectString;

    private String namespace;

    private int sessionTimeout = 5000;

    private int connectionTimeout = 3000;

    private RetryPolicy retryPolicy = new RetryForever(30000);

    public ZkConnection(String connectString) {
        this.connectString = connectString;
        this.namespace = "";
    }

    public ZkConnection(String connectString, String namespace) {
        this.connectString = connectString;
        this.namespace = namespace;
    }

    public CuratorFramework connect() {
        curator = CuratorFrameworkFactory.builder().connectString(connectString)
                .sessionTimeoutMs(sessionTimeout)
                .connectionTimeoutMs(connectionTimeout)
                .retryPolicy(retryPolicy)
                .namespace(namespace)
                .build();
        return curator;
    }

    public void destroy() {
        curator.close();
    }
}
