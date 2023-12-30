package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/28 14:55
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService
{

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    /**
     * 课程分页查询
     * @param pageParams 查询页码 每页个数
     * @param queryCourseParamsDto 查询参数
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto)
    {
        // 拼装查询条件
        LambdaQueryWrapper<CourseBase> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StringUtils.hasText(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        lqw.like(StringUtils.hasText(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        lqw.eq(StringUtils.hasText(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        // 分页
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        courseBaseMapper.selectPage(page, lqw);

        // 封装数据
        PageResult<CourseBase> pageResult = PageResult.<CourseBase>builder()
                .items(page.getRecords())
                .counts(page.getTotal())
                .page(pageParams.getPageNo())
                .pageSize(pageParams.getPageSize())
                .build();

        return pageResult;
    }
}
