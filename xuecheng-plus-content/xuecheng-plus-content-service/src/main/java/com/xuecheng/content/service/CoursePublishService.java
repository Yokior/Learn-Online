package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/29 15:15
 */
public interface CoursePublishService
{
    CoursePreviewDto getCoursePreviewInfo(Long courseId);
}
