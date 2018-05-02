package com.lanxiang.zk.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by lanxiang on 2018/5/2.
 */

@SpringBootApplication
@ComponentScan(basePackages = "com.lanxiang.zk.practice")
@ImportResource(locations = {"classpath:applicationContext.xml"})
public class AppBootStrap {

    public static void main(String[] args) {
        SpringApplication.run(AppBootStrap.class, args);
    }

}
