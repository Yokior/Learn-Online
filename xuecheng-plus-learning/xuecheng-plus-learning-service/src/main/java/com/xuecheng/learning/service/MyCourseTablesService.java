package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/13 15:53
 */
public interface MyCourseTablesService
{
    /**
     *  添加选课信息
     * @param userId
     * @param courseId
     * @return
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);
}
