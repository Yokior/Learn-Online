package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.CoursePublish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程发布(CoursePublish)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-27 14:54:13
 */
@Mapper
public interface CoursePublishMapper extends BaseMapper<CoursePublish> {

}

