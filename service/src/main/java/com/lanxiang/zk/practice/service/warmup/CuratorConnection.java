package com.lanxiang.zk.practice.service.warmup;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.BoundedExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * Created by lanxiang on 2018/5/17.
 */
public class CuratorConnection {

    private static final int SESSION_TIMEOUT = 5000;

    private static final int CONNECT_TIMEOUT = 5000;

    private CuratorFramework curator;

    public void connection(String hosts) throws Exception {
        curator = CuratorFrameworkFactory.builder().connectString(hosts)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .connectionTimeoutMs(CONNECT_TIMEOUT)
                .retryPolicy(new BoundedExponentialBackoffRetry(1000, 15000, 3))
                .build();
        curator.start();
    }

    public void createNode() throws Exception {
        String notePath = curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/home/lanxiang");
        System.out.println("created : " + notePath);
    }
}
