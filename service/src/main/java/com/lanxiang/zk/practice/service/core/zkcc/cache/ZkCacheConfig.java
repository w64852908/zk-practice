package com.lanxiang.zk.practice.service.core.zkcc.cache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by lanxiang on 2018/5/27.
 */

/**
 * 本地缓存，记录每次配置信息发生的变更以及数据
 */
public class ZkCacheConfig implements Serializable {

    private String nodeName;

    //key->zk节点的绝对路径 value->节点上的数据
    private ConcurrentMap<String, String> config;

    private Long version;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public ConcurrentMap<String, String> getConfig() {
        return config;
    }

    public void setConfig(ConcurrentMap<String, String> config) {
        this.config = config;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
