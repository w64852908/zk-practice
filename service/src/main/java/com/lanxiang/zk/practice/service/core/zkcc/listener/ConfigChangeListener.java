package com.lanxiang.zk.practice.service.core.zkcc.listener;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanxiang.zk.practice.common.utils.FieldValueTransUtil;
import com.lanxiang.zk.practice.service.annotation.ZkConfig;

/**
 * Created by lanxiang on 2018/5/27.
 */
//对zk上对应配置项的监听者，当一个key发生change事件时，更新对应数据
public class ConfigChangeListener implements IConfigChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeListener.class);

    private Class<?> clazz;

    public ConfigChangeListener(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void changed(String key, String oldValue, String newValue) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.isAnnotationPresent(ZkConfig.class)) {
                    ZkConfig zkConfig = field.getAnnotation(ZkConfig.class);
                    if (key.equals(zkConfig.key())) {
                        field.setAccessible(true);
                        field.set(clazz, FieldValueTransUtil.transferValue(field, newValue));
                        LOGGER.info("config listener : [" + clazz.getSimpleName() + "." + field.getName() + "] updated, {} -> {}", oldValue, newValue);
                    }
                }
            } catch (Exception e) {
                LOGGER.info("update config value failed, cause : {}", e);
            }
        }
    }


}
