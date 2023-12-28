package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description：启动类
 * @Auther：Yokior
 * @Date：2023/12/26 17:43
 */
@EnableSwagger2Doc
@SpringBootApplication
public class ContentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ContentApplication.class, args);
    }
}
