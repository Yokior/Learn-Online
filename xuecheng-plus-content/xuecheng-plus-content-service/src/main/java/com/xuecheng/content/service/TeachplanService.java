package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/5 14:54
 */
public interface TeachplanService
{
    // 根据课程Id获取课程计划树
    List<TeachplanDto> getTreeNodes(Long courseId);

    // 新增 修改课程计划
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    // 教学计划绑定媒资
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);
}
