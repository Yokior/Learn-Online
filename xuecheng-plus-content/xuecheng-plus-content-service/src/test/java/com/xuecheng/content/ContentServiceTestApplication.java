package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.entity.CourseBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/27 15:03
 */
@SpringBootTest
public class ContentServiceTestApplication
{

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 分页查询测试
     */
    @Test
    public void testCourseBaseMapper()
    {
        // 查询条件
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(CourseBase::getAuditStatus, "202004");

        // 创建page分页
        Page<CourseBase> courseBasePage = new Page<>(1,2);
        courseBaseMapper.selectPage(courseBasePage, lqw);

        List<CourseBase> courseBaseList = courseBasePage.getRecords();
        // 封装数据
        PageResult<CourseBase> pageResult = PageResult.<CourseBase>builder()
                .items(courseBaseList)
                .page((long) 1)
                .pageSize((long) 2)
                .counts(courseBasePage.getTotal())
                .build();

        System.out.println(pageResult);

    }
}
