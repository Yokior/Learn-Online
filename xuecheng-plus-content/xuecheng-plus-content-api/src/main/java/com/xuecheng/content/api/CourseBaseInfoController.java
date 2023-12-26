package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.entity.CourseBase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/26 17:39
 */
@RestController
@RequestMapping("/course")
public class CourseBaseInfoController
{
    /**
     * 分页条件查询
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @PostMapping("/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto)
    {
        return null;
    }
}
