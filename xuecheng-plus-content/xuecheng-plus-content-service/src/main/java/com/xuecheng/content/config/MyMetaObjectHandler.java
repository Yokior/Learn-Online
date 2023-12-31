package com.xuecheng.content.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler
{
    @Override
    public void insertFill(MetaObject metaObject)
    {
        this.setFieldValByName("createDate", LocalDateTime.now(), metaObject);
        this.setFieldValByName("changeDate", LocalDateTime.now(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject)
    {
        this.setFieldValByName("changeDate", LocalDateTime.now(), metaObject);
    }
}