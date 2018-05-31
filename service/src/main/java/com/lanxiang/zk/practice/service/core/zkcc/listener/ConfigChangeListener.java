package com.lanxiang.zk.practice.service.core.zkcc.listener;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanxiang.zk.practice.common.exception.PracticeException;
import com.lanxiang.zk.practice.service.annotation.ZkConfig;

/**
 * Created by lanxiang on 2018/5/27.
 */

//ZkConfig注解标记静态成员属性的监听器，触发changed方法更新其值
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
                    //只有发生变更的key和注解上标注的key相等时，才更新其值
                    if (key.equals(zkConfig.key())) {
                        field.setAccessible(true);
                        field.set(clazz, transferValue(field, newValue));
                        LOGGER.info("config listener : [" + clazz.getSimpleName() + "." + field.getName() + "] updated, [{}] -> [{}]", oldValue, newValue);
                    }
                }
            } catch (Exception e) {
                LOGGER.info("update config value failed, cause : {}", e);
            }
        }
    }

    private Object transferValue(Field field, String value) throws PracticeException {
        Object result;
        String fieldName = field.getDeclaringClass().getName() + "." + field.getName();
        String typeName = field.getType().getSimpleName();
        try {
            if (StringUtils.equals(typeName, "String")) {
                result = value;
            } else if (StringUtils.equals(typeName, "int") || StringUtils.equals(typeName, "Integer")) {
                result = Integer.valueOf(value);
            } else if (StringUtils.equals(typeName, "long") || StringUtils.equals(typeName, "Long")) {
                result = Long.valueOf(value);
            } else if (StringUtils.equals(typeName, "float") || StringUtils.equals(typeName, "Float")) {
                result = Float.valueOf(value);
            } else if (StringUtils.equals(typeName, "double") || StringUtils.equals(typeName, "Double")) {
                result = Double.valueOf(value);
            } else if (StringUtils.equals(typeName, "boolean") || StringUtils.equals(typeName, "Boolean")) {
                result = Boolean.valueOf(value);
            } else {
                throw new PracticeException("UNEXPECTED FIELD (" + fieldName + ") TYPE : TYPE IS " + typeName);
            }
        } catch (Exception e) {
            throw new PracticeException("transfer value for " + fieldName + " failed", e);
        }
        return result;
    }
}
