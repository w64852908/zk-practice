package com.lanxiang.zk.practice.service.core.zkcc;

import java.util.Map;
import java.util.Set;

import com.lanxiang.zk.practice.service.core.zkcc.listener.IConfigChangeListener;

/**
 * Created by lanxiang on 2018/5/26.
 */
public interface ZkConfigClientInvoker {

    void init();

    void destroy();

    void addListener(String key, IConfigChangeListener listener);

    String getValue(String key);

    Map<String, String> getAllKeyValues();

    Set<String> getAllKeys();

    Boolean setValue(String key, String value);

    void setPullPeriod(long pullPeriod);

    void setScanBasePackage(String scanBasePackage);

    void setId(String id);

}
