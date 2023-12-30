package com.xuecheng.system;

import com.xuecheng.system.model.po.Dictionary;
import com.xuecheng.system.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/29 17:01
 */
@SpringBootTest
public class SystemTestApplication
{
    @Autowired
    private DictionaryService dictionaryService;

    @Test
    public void testService()
    {
        List<Dictionary> dictionaryList = dictionaryService.queryAll();
        System.out.println(dictionaryList);
    }

}
