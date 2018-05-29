package com.lanxiang.zk.practice.service.core.zkcc;

import java.util.Map;
import java.util.Set;

import com.lanxiang.zk.practice.service.core.zkcc.listener.IConfigChangeListener;

/**
 * Created by lanxiang on 2018/5/26.
 */
public interface ZkConfigClientInvoker {

    void init();

    String getValue(String key);

    Map<String, String> getAllKeyValues();

    Set<String> getAllKeys();

    boolean setValue(String key, String value);

    void setScanBasePackage(String scanBasePackage);
}
