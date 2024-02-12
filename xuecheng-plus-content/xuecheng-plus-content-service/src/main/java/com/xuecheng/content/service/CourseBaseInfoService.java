package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.entity.CourseBase;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/28 14:51
 */
public interface CourseBaseInfoService
{
    // 课程分页查询
    PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    // 新增课程
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    // 根据Id返回
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    // 修改信息
    CourseBaseInfoDto updateCourseBaseInfo(Long companyId, EditCourseDto editCourseDto);
}
