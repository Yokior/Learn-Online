package com.xuecheng.content;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/30 17:32
 */
@SpringBootTest
public class CourseCategoryTest
{

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testTreeNodes()
    {

    }

}
