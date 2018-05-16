package com.lanxiang.zk.practice.service.warmup;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by lanxiang on 2018/5/16.
 */
public class ConnnectionWatcher implements Watcher {

    private static final int SESSION_TIMEOUT = 5000;

    protected ZooKeeper zk = null;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void process(WatchedEvent watchedEvent) {
        Event.KeeperState state = watchedEvent.getState();

        if (state == Event.KeeperState.SyncConnected) {
            latch.countDown();
            System.out.println("zookeeper sync conntected.");
        }
    }

    public void connection(String hosts) throws IOException, InterruptedException {
        zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
        System.out.println("await zookeeper sync connected.");
        latch.await();
    }

    public void close() throws InterruptedException {
        if (null != zk) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                throw e;
            } finally {
                zk = null;
                System.out.println("zookeeper connection closed.");
                System.gc();
            }
        }
    }

}
