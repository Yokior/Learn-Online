package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/30 17:02
 */
public interface CourseCategoryService
{
    // 课程分类树形结构
    List<CourseCategoryTreeDto> queryTreeNodes();
}
