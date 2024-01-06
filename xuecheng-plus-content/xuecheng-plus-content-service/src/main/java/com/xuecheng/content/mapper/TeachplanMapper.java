package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.Teachplan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程计划(Teachplan)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-27 14:54:13
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan>
{
    // 获取同级orderby的最大值
    Integer getOrderbyMax(@Param("parentId") Long parentId, @Param("courseId") Long courseId);
}

