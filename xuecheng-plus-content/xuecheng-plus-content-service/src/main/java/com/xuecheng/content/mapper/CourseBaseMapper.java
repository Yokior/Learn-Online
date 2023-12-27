package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.CourseBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程基本信息(CourseBase)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-27 14:54:11
 */
@Mapper
public interface CourseBaseMapper extends BaseMapper<CourseBase> {

}

