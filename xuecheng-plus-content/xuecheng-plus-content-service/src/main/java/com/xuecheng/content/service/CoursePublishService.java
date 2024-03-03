package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.entity.CoursePublish;

import java.io.File;

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
     *
     * @param companyId
     * @param courseId
     */
    void publish(Long companyId, Long courseId);

    /**
     * @param courseId 课程id
     * @return File 静态化文件
     * @description 课程静态化
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    File generateCourseHtml(Long courseId);

    /**
     * @param file 静态化文件
     * @return void
     * @description 上传课程静态化页面
     * @author Mr.M
     * @date 2022/9/23 16:59
     */
    void uploadCourseHtml(Long courseId, File file);

    /**
     * 查询课程发布信息
     * @param courseId
     * @return
     */
    CoursePublish getCoursePublish(Long courseId);

    /**
     * 查询课程发布信息 缓存
     * @param courseId
     * @return
     */
    CoursePublish getCoursePublishCache(Long courseId);
}
