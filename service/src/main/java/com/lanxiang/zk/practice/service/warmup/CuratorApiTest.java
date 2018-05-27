package com.lanxiang.zk.practice.service.warmup;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.lanxiang.zk.practice.service.common.ZkConnection;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class CuratorApiTest {

    private String hosts = "127.0.0.1:2181";

    private ZooKeeper zk;

    @Before
    public void init() throws Exception {
        zk = new ZooKeeper(hosts, 5000, new ConnnectionWatcher());
    }

    @After
    public void end() throws Exception {
        System.out.println();
        System.out.println("==================zk node tree==================");
//        System.out.println(ZkUtilCopy.listSubTreeBFS(zk, "/lanxiang"));
    }

    @Test
    public void testRewatch() throws Exception {
        ZkConnection zkConnection = new ZkConnection(hosts);
        CuratorFramework curator = zkConnection.connect();

        String nodePath = "/lanxiang/learn/zk";

        //初始化connection
        curator.start();

        //节点监听事件
        createNodeListener(curator, "/lanxiang/learn");
        //子节点监听事件
        createChildNodeListener(curator, "/lanxiang");

        //创建节点
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(nodePath, "{\"name\",\"lan\"}".getBytes());


        Stat stat = new Stat();

        //读取数据，把节点状态存储到stat中
        String data = new String(curator.getData().storingStatIn(stat).forPath("/lanxiang/learn/zk"));
        System.out.println("content: " + data + ", stat : " + JSON.toJSONString(stat));

        //更新数据
        Stat beforeStat = new Stat();
        String beforeData = new String(curator.getData().storingStatIn(beforeStat).forPath("/lanxiang/learn"));
        curator.setData().withVersion(beforeStat.getVersion()).forPath("/lanxiang/learn", "i am father".getBytes());
        String afterData = new String(curator.getData().forPath("/lanxiang/learn"));
        System.out.println("before data : " + beforeData + ", after data : " + afterData);

        curator.setData().forPath("/lanxiang/learn", "i am grand father".getBytes());
        curator.setData().forPath("/lanxiang/learn", "i am grand grand father".getBytes());

        //删除节点
        curator.delete().deletingChildrenIfNeeded().withVersion(stat.getVersion()).forPath("/lanxiang");
    }

    //创建节点listener
    private void createNodeListener(CuratorFramework curator, String path) throws Exception {
        final NodeCache cache = new NodeCache(curator, path, false);
        cache.start(true);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                if (null != cache.getCurrentData()) {
                    System.out.println(String.format("node data updated, current data : %s", new String(cache.getCurrentData().getData())));
                }
            }
        });
    }

    private void createChildNodeListener(CuratorFramework curator, String path) throws Exception {
        final PathChildrenCache cache = new PathChildrenCache(curator, path, true);
        cache.start();
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_UPDATED:
                        System.out.println("child_updated, path : " + event.getData().getPath() + ", data : " + new String(event.getData().getData()));
                        break;
                    default:
                        System.out.println("child event : " + event.getType().name() + ", path : " + event.getData().getPath());
                }
            }
        });
    }
}
