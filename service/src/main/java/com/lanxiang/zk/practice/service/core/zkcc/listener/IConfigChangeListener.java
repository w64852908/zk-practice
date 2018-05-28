package com.lanxiang.zk.practice.service.core.zkcc.listener;

/**
 * Created by lanxiang on 2018/5/27.
 */
public interface IConfigChangeListener {

    void changed(String key, String oldValue, String newValue);

}
