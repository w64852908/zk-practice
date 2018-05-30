package com.lanxiang.zk.practice.service.core.nameservice;

import com.lanxiang.zk.practice.service.common.ZkConnection;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by lanjing on 2018/5/30.
 */

@Service
public class GUIDGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GUIDGenerator.class);

    private static final String connectString = "127.0.0.1:2181";

    private static final String namespace = "zk-practice.guidgenerator";

    private static final String rootPath = "/guid/";

    private CuratorFramework curator;

    private boolean fastMode;

    @PostConstruct
    public void init() {
        ZkConnection zkConnection = new ZkConnection(connectString, namespace);
        curator = zkConnection.connect();
        curator.start();
        fastMode = true;
    }

    public String generate(String node) {
        String path = rootPath + node + "-";
        try {
            //创建持久的顺序节点
            path = curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(path);
            String result = retrieveGUID(path, node);
            //删除掉该持久节点
            if (fastMode) {
                final String finalPath = path;
                new Thread(() -> {
                    try {
                        curator.delete().forPath(finalPath);
                    } catch (Exception e) {
                        LOGGER.error("delete guid persistent sequential node failed, {}", e);
                    }
                }).start();
            } else {
                curator.delete().forPath(path);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("generate guid failed, {}", e);
            throw new IllegalStateException("generate guid failed", e);
        }
    }

    //新节点path的最后一段即是生产的全局唯一顺序自增的id
    private String retrieveGUID(String path, String node) {
        return path.substring(path.lastIndexOf(node) + node.length() + 1);
    }
}
