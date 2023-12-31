package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程分页查询
     *
     * @param pageParams           查询页码 每页个数
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

    /**
     * 新增课程
     *
     * @param dto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto)
    {
        // 数据校验
        //合法性校验
        if (StringUtils.isEmpty(dto.getName()))
        {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isEmpty(dto.getMt()))
        {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isEmpty(dto.getSt()))
        {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isEmpty(dto.getGrade()))
        {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isEmpty(dto.getTeachmode()))
        {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isEmpty(dto.getUsers()))
        {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isEmpty(dto.getCharge()))
        {
            throw new RuntimeException("收费规则为空");
        }

        // 向course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);

        courseBaseNew.setCompanyId(companyId);

        // 审核状态默认未提交
        courseBaseNew.setAuditStatus(SystemCommon.AUDIT_NOT_PUBLISH);
        // 发布状态定义为未发布
        courseBaseNew.setStatus(SystemCommon.STATUS_NOT_PUBLISH);

        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0)
        {
            throw new RuntimeException("课程基本信息添加失败");
        }

        // 向course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto, courseMarketNew);
        // 课程的Id
        courseBaseNew.setId(courseId);
        int save = saveCourseMarket(courseMarketNew);
        if (save <= 0)
        {
            throw new RuntimeException("课程营销信息添加/更新失败");
        }

        return getCourseBaseInfo(courseId);
    }


    /**
     * 获取课程基本信息dto
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId)
    {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null)
        {
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 组装
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);

        // 获取课程分类名称
        LambdaQueryWrapper<CourseCategory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(StringUtils.hasText(courseBaseInfoDto.getSt()),CourseCategory::getId,courseBaseInfoDto.getSt());
        CourseCategory stCategory = courseCategoryMapper.selectOne(lqw);
        courseBaseInfoDto.setStName(stCategory.getName());

        lqw = new LambdaQueryWrapper<CourseCategory>();
        lqw.eq(StringUtils.hasText(courseBaseInfoDto.getMt()),CourseCategory::getId,courseBaseInfoDto.getMt());
        CourseCategory mtCategory = courseCategoryMapper.selectOne(lqw);
        courseBaseInfoDto.setMtName(mtCategory.getName());

        return courseBaseInfoDto;
    }



    /**
     * 保存营销信息： course_base存在则更新 不存在则创建
     * @param courseMarket
     * @return
     */
    private int saveCourseMarket(CourseMarket courseMarket)
    {
        // 数据的合法性校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge))
        {
            throw new RuntimeException("收费规则为空");
        }

        // 课程收费却没有价格
        if (charge.equals(SystemCommon.COURSE_CHARGE))
        {
            Float price = (Float) courseMarket.getPrice();
            if (price == null || price <= 0)
            {
                throw new RuntimeException("收费课程价格必须大于0");
            }
        }

        // 查找课程营销数据
        CourseMarket dbCourseMarket = courseMarketMapper.selectById(courseMarket.getId());

        if (dbCourseMarket == null)
        {
            // 不存在 则创建
            int insert = courseMarketMapper.insert(courseMarket);
            return insert;
        }
        else
        {
            // 存在 更新数据
            BeanUtils.copyProperties(dbCourseMarket,courseMarket);
            int update = courseMarketMapper.updateById(courseMarket);
            return update;
        }

    }


}
