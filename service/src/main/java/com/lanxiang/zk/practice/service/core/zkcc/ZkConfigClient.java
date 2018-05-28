package com.lanxiang.zk.practice.service.core.zkcc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.zookeeper.CreateMode;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    //缓存client的当前配置数据
    private ZkCacheConfig cacheConfig;

    /**
     * key->zk配置信息节点的path value->该节点对应的ConfigChangeListener
     * 节点path和成员属性监听器映射
     */
    private Map<String, IConfigChangeListener> listenerMap = new HashMap<>();

    @Override
    public void init() {
        if (StringUtils.isBlank(nodeName)) {
            throw new IllegalArgumentException("nodeName不能为空或重复");
        }
        if (StringUtils.isBlank(connectString)) {
            throw new IllegalArgumentException("zk host 为空");
        }
        //初始化client缓存
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
        //扫描scanBasePackage下所有类的带有ZkConfig注解的成员属性
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(scanBasePackage))
                .setScanners(new FieldAnnotationsScanner()));
        Set<Field> fields = reflections.getFieldsAnnotatedWith(ZkConfig.class);
        //记录配置client时的key，防止重复配置项
        Set<String> configKeySet = new HashSet<>();
        //连接zk，初始化curator
        ZkConnection connection = new ZkConnection(connectString, appkey);
        CuratorFramework curator = connection.connect();
        curator.start();
        //key clientId value 对应的子节点监听者
        for (Field field : fields) {
            if (!field.isAnnotationPresent(ZkConfig.class)) {
                continue;
            }

            ZkConfig zkConfig = field.getAnnotation(ZkConfig.class);

            //ZkConfig注解上key和clientId的内容
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

            //拼出该field在zk上节点的绝对路径
            String keyPath = path + nodeName + "/" + clientId + "/" + key;

            //如果该节点已存在，则放进cacheConfig，如果不存在则创建zk上的临时节点
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
            //给成员属性初始化值，并创建其监听器
            addToListenerMap(keyPath, zkConfig, field, nodeData);

            //创建zk上该节点缓存
            final NodeCache nodeCache = new NodeCache(curator, keyPath);
            try {
                nodeCache.start(true);
            } catch (Exception e) {
                throw new PracticeException("init zk config node listener failed.");

            }

            //添加节点更新事件的监听器
            nodeCache.getListenable().addListener(() -> {
                //当节点更新后的data不为空时，才更新本地配置
                if (null != nodeCache.getCurrentData() && null != nodeCache.getCurrentData().getData()) {
                    //获取更新节点的绝对路径，对应cacheConfig中缓存的key和ZkConfig注解成员属性监听器映射的key
                    String listenerKey = nodeCache.getCurrentData().getPath();
                    /**
                     *
                     * ZkConfig注解的key，对应zk节点的最后一级相对路径↓↓↓
                     * path + nodeName + "/" + clientId + "/" + key;
                     * String key = zkConfig.key();
                     */
                    String annotatedKey = listenerKey.substring(listenerKey.lastIndexOf("/") + 1);
                    //从本地缓存中取出更新前的值
                    String oldValue = cacheConfig.getConfig().get(listenerKey);
                    //节点当前值
                    String newValue = new String(nodeCache.getCurrentData().getData());
                    //更新成员属性的值
                    listenerMap.get(listenerKey).changed(annotatedKey, oldValue, newValue);
                    //更新本地缓存中的值和version版本号
                    cacheConfig.getConfig().put(listenerKey, newValue);
                    cacheConfig.setVersion(cacheConfig.getVersion() + 1);
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