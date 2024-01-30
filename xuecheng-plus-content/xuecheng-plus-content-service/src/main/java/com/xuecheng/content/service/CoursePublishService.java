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

    void commitAudit(Long companyId, Long courseId);

    /**
     * 课程发布
     * @param companyId
     * @param courseId
     */
    void publish(Long companyId, Long courseId);
}
