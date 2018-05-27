package com.lanxiang.zk.practice.service.executetest.zkconfig;

import com.lanxiang.zk.practice.service.annotation.ZkConfig;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class LanxiangConfig {

    @ZkConfig(clientId = "lanxiangZkConfig", key = "lanxiang.name")
    private static String name;

    @ZkConfig(clientId = "lanxiangZkConfig", key = "lanxiang.age")
    private static Integer age;

    public static String getName() {
        return name;
    }

    public static Integer getAge() {
        return age;
    }
}
