package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.content.model.entity.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/13 15:53
 */
public class MyCourseTablesServiceImpl implements MyCourseTablesService
{

    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Transactional
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId)
    {
        // 选课调用内容管理查询收费规则
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null)
        {
            XueChengPlusException.cast("课程信息不存在");
        }

        // 收费规则
        String charge = coursepublish.getCharge();
        if (SystemCommon.COURSE_FREE.equals(charge))
        {
            // 免费
            // 选课记录表写入信息
            XcChooseCourse xcChooseCourse = addFreeCourse(userId, coursepublish);
            // 我的课程表写入信息
            XcCourseTables xcCourseTables = addCourseTables(xcChooseCourse);
        }
        else
        {
            // 收费
            XcChooseCourse xcChooseCourse = addChargeCourse(userId, coursepublish);
        }

        // 判断学生的学习资格


        return null;
    }


    // 添加免费课程
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish)
    {
        // 判断 如果已经存在免费的选课记录直接返回
        Long courseId = coursePublish.getId();
        LambdaQueryWrapper<XcChooseCourse> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcChooseCourse::getUserId, userId);
        lqw.eq(XcChooseCourse::getCourseId, courseId);
        lqw.eq(XcChooseCourse::getOrderType, "700001"); // 免费课程
        lqw.eq(XcChooseCourse::getStatus, "701001"); // 选课成功

        List<XcChooseCourse> xcChooseCourseList = xcChooseCourseMapper.selectList(lqw);
        if (xcChooseCourseList.size() > 0)
        {
            return xcChooseCourseList.get(0);
        }

        // 向选课记录表添加一条记录
        XcChooseCourse xcChooseCourse = new XcChooseCourse();

        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCourseId(courseId);
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        xcChooseCourse.setValidDays(365);
        xcChooseCourse.setStatus("701001");
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insert <= 0)
        {
            XueChengPlusException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }

    // 添加到我的课程
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse)
    {
        // 选课成功才能向我的课程表添加记录
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status))
        {
            XueChengPlusException.cast("选课没有成功无法添加到课程表");
        }

        // 如果已经存在 直接返回
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables != null)
        {
            return xcCourseTables;
        }

        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourse);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());

        int insert = xcCourseTablesMapper.insert(xcCourseTables);
        if (insert <= 0)
        {
            XueChengPlusException.cast("添加到课程表失败");
        }


        return xcCourseTables;
    }

    // 添加收费课程
    public XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish)
    {
        return null;
    }

    /**
     * @param userId
     * @param courseId
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId, Long courseId)
    {
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }


}
