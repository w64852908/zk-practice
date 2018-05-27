package com.lanxiang.zk.practice.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lanxiang on 2018/5/27.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ZkConfig {

    //ZkConfigClient的唯一标识
    String clientId();

    //配置项的key
    String key();
}
