package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/29 15:16
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService
{

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId)
    {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);

        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);
        coursePreviewDto.setTeachplans(treeNodes);

        return coursePreviewDto;
    }

    /**
     * 提交审核
     * @param companyId
     * @param courseId
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId)
    {
        // 查询状态：已提交则不允许提交
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null)
        {
            XueChengPlusException.cast("课程信息不存在");
        }
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (SystemCommon.AUDIT_IS_PUBLISH.equals(auditStatus))
        {
            XueChengPlusException.cast("课程已提交，等待审核结果");
        }

        // 课程图片 计划信息没有填写不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic))
        {
            XueChengPlusException.cast("课程图片不能为空");
        }
        List<TeachplanDto> teachPlanTree = teachplanService.getTreeNodes(courseId);
        if (teachPlanTree.isEmpty())
        {
            XueChengPlusException.cast("课程计划不能为空");
        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();

        // 查询课程基本信息
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);

        // 查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null)
        {
            XueChengPlusException.cast("营销信息不存在");
        }
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        // 查询计划信息
        String teachPlanJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanJson);

        // 设置机构的ID
        coursePublishPre.setCompanyId(companyId);

        // TODO: 本机构只能发布自己的视频

        // 查询预发布表 如果有就更新 没有就插入
        CoursePublishPre dbCoursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (dbCoursePublishPre == null)
        {
            coursePublishPreMapper.insert(coursePublishPre);
        }
        else
        {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本信息为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus(SystemCommon.AUDIT_IS_PUBLISH);
        courseBaseMapper.updateById(courseBase);
    }
}
