package com.lanxiang.zk.practice.service.executetest.zkconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import com.lanxiang.zk.practice.service.core.zkcc.ZkConfigClient;

/**
 * Created by lanxiang on 2018/5/27.
 */
@Component
public class ZkConfigClientBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkConfigClientBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        ZkConfigClient client = new ZkConfigClient();
        client.setAppkey("com.lanxiang.zk.practice");
        client.setNodeName("lanxiangZkPractice");
        client.setScanBasePackage("com.lanxiang.zk.practice.service.executetest.zkconfig");
        client.setConnectString("127.0.0.1:2181");
        client.init();
        configurableListableBeanFactory.registerSingleton("zkConfigClient", client);
    }
}
