package com.lanxiang.zk.practice.service.core.zkcc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.lanxiang.zk.practice.common.exception.PracticeException;
import com.lanxiang.zk.practice.service.annotation.ZkConfig;
import com.lanxiang.zk.practice.service.common.ZkConnection;
import com.lanxiang.zk.practice.service.core.zkcc.cache.ZkCacheConfig;
import com.lanxiang.zk.practice.service.core.zkcc.listener.ConfigChangeListener;
import com.lanxiang.zk.practice.service.core.zkcc.listener.IConfigChangeListener;

/**
 * Created by lanxiang on 2018/5/26.
 */
public class ZkConfigClient implements ZkConfigClientInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkConfigClient.class);

    //注册到zk的节点名称
    private String nodeName;

    //服务名（拿来当namespace用？）
    private String appkey;

    //扫描client端的配置信息
    private String scanBasePackage;

    //在zk上node的path
    private String path = "/";

    //zk的host
    private String connectString;

    private ZkCacheConfig cacheConfig;

    private Map<String, IConfigChangeListener> listenerMap = new HashMap<>();

    //key clientId value 对应的子节点监听者
    private Map<String, PathChildrenCache> pathChildrenCacheMap = new HashMap<>();


    @Override
    public void init() {
        if (StringUtils.isBlank(nodeName)) {
            throw new IllegalArgumentException("nodeName不能为空或重复");
        }
        if (StringUtils.isBlank(connectString)) {
            throw new IllegalArgumentException("zk host 为空");
        }
        initCacheConfig();
        //扫描对应basePackage下所有的动态配置项，并监听其节点并初始化
        scanConfigAnnotationsAndRegisterNodes();
        LOGGER.info("zk config client init finished.");
    }

    private void initCacheConfig() {
        cacheConfig = new ZkCacheConfig();
        cacheConfig.setNodeName(nodeName);
        cacheConfig.setVersion(0L);
        cacheConfig.setConfig(new ConcurrentHashMap<>());
    }

    private void scanConfigAnnotationsAndRegisterNodes() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(scanBasePackage))
                .setScanners(new FieldAnnotationsScanner()));

        Set<Field> fields = reflections.getFieldsAnnotatedWith(ZkConfig.class);
        Set<String> configKeySet = new HashSet<>();
        ZkConnection connection = new ZkConnection(connectString, appkey);
        CuratorFramework curator = connection.connect();
        curator.start();
        //key clientId value 对应的子节点监听者
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ZkConfig.class)) {
                continue;
            }
            ZkConfig zkConfig = field.getAnnotation(ZkConfig.class);

            String key = zkConfig.key();
            String clientId = zkConfig.clientId();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(clientId)) {
                continue;
            }
            //如果配置的key已经存在
            if (configKeySet.contains(key)) {
                throw new PracticeException("found same config key");
            } else {
                configKeySet.add(key);
            }
            String clientIdPath = path + nodeName + "/" + clientId;
            String keyPath = clientIdPath + "/" + key;

            boolean isNodeExist;
            String nodeData = null;
            try {
                isNodeExist = null != curator.checkExists().forPath(keyPath);
                if (isNodeExist) {
                    nodeData = new String(curator.getData().forPath(keyPath));
                } else {
                    curator.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(keyPath);
                }
            } catch (Exception e) {
                LOGGER.error("check node stat and create if not exist error : {}", e);
            }
            if (StringUtils.isNotBlank(nodeData)) {
                cacheConfig.getConfig().put(keyPath, nodeData);
            }
            addToListenerMap(keyPath, zkConfig, field, nodeData);
            final PathChildrenCache childrenCache;
            if (pathChildrenCacheMap.containsKey(clientIdPath)) {
                childrenCache = pathChildrenCacheMap.get(clientIdPath);
            } else {
                childrenCache = new PathChildrenCache(curator, clientIdPath, true);
                try {
                    childrenCache.start();
                } catch (Exception e) {
                    throw new PracticeException("init zk config node listener failed.");
                }
                pathChildrenCacheMap.put(clientIdPath, childrenCache);
            }
            childrenCache.getListenable().addListener((client, event) -> {
                LOGGER.info("received event : {}", JSON.toJSONString(event));
                switch (event.getType()) {
                    case CHILD_ADDED:
                        String initValue = new String(event.getData().getData());
                        listenerMap.get(keyPath).changed(key, "", initValue);
                        cacheConfig.getConfig().put(keyPath, initValue);
                        cacheConfig.setVersion(cacheConfig.getVersion() + 1);
                        break;
                    case CHILD_UPDATED:

                        String oldValue = cacheConfig.getConfig().get(keyPath);
                        String newValue = new String(event.getData().getData());
                        listenerMap.get(keyPath).changed(key, oldValue, newValue);
                        cacheConfig.getConfig().put(keyPath, newValue);
                        cacheConfig.setVersion(cacheConfig.getVersion() + 1);
                        break;
                }
            });
        }
    }


    private void addToListenerMap(String keyPath, ZkConfig zkConfig, Field field, String data) {
        if (!listenerMap.containsKey(keyPath)) {
            IConfigChangeListener listener = new ConfigChangeListener(field.getDeclaringClass());
            if (StringUtils.isNotBlank(data)) {
                listener.changed(zkConfig.key(), "", data);
                cacheConfig.getConfig().put(zkConfig.key(), data);
            }
            listenerMap.put(keyPath, listener);
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void addListener(String key, IConfigChangeListener listener) {

    }

    @Override
    public String getValue(String key) {
        return null;
    }

    @Override
    public Map<String, String> getAllKeyValues() {
        return null;
    }

    @Override
    public Set<String> getAllKeys() {
        return null;
    }

    @Override
    public Boolean setValue(String key, String value) {
        return null;
    }

    @Override
    public void setPullPeriod(long pullPeriod) {

    }

    public void setScanBasePackage(String scanBasePackage) {
        this.scanBasePackage = scanBasePackage;
    }

    @Override
    public void setId(String id) {

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getScanBasePackage() {
        return scanBasePackage;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }
}