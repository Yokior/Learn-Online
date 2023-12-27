package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.entity.Teachplan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程计划(Teachplan)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-27 14:54:13
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

}

