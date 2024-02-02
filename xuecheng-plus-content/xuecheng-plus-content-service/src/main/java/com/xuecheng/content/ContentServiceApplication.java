package com.xuecheng.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description：启动类
 * @Auther：Yokior
 * @Date：2023/12/26 17:43
 */
@SpringBootApplication(scanBasePackages = {"com.xuecheng.content","com.xuecheng.messagesdk"})
//@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
public class ContentServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
