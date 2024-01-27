package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/5 14:55
 */
@Service
public class TeachplanServiceImpl implements TeachplanService
{

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    // 获取所有数据
    private List<Teachplan> dbTeachplanList;

    private List<TeachplanMedia> dbTeachplanMediaList;

    /**
     * 根据课程id获取课程计划树
     *
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId)
    {
        // 排序
        LambdaQueryWrapper<Teachplan> lqw = new LambdaQueryWrapper<>();
        lqw.orderByAsc(Teachplan::getCourseId);
        lqw.orderByAsc(Teachplan::getOrderby);

        this.dbTeachplanList = teachplanMapper.selectList(lqw);
        this.dbTeachplanMediaList = teachplanMediaMapper.selectList(null);

        if (dbTeachplanList.isEmpty() || dbTeachplanMediaList.isEmpty())
        {
            XueChengPlusException.cast("获取课程计划数据异常请重试");
        }

        // 构造人造根节点
        TeachplanDto teachplanDto = new TeachplanDto();
        teachplanDto.setId(0L);

        // 递归构造子节点数据
        buildChildrenNodes(courseId, teachplanDto);

        // 去除人造根节点
        return teachplanDto.getTeachPlanTreeNodes();
    }

    /**
     * 新增 修改 课程计划
     *
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto)
    {
        // 根据课程id判断是新增还是修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null)
        {
            // 新增操作
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            // 确定排序字段
            Long parentid = teachplan.getParentid();

            Integer max = teachplanMapper.getOrderbyMax(parentid, teachplan.getCourseId());
            // 如果max为空 默认为1 (0+1)
            if (max == null)
            {
                max = 0;
            }
            teachplan.setOrderby(max + 1);

            teachplanMapper.insert(teachplan);
        }
        else
        {
            // 修改操作
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            if (teachplan == null)
            {
                XueChengPlusException.cast("课程计划不存在");
            }
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }


    }

    /**
     * 教学计划和媒资文件的关联
     * @param bindTeachplanMediaDto
     */
    @Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto)
    {
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null)
        {
            XueChengPlusException.cast("课程计划不存在");
        }

        // 先删除原有记录 根据课程计划id删除绑定的媒资
        int delete = teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId, teachplanId));

        // 再添加记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    /**
     * 根据课程计划id构造子节点树
     *
     * @param courseId
     * @param head
     */
    private void buildChildrenNodes(Long courseId, TeachplanDto head)
    {
        Long teachplanId = head.getId();

        // 根据父节点的Id查询子节点
        List<Teachplan> teachplanList = dbTeachplanList.stream()
                .filter(t -> t.getParentid().equals(teachplanId) && t.getCourseId().equals(courseId))
                .collect(Collectors.toList());

        // 没有子节点 处于最后一层 封装媒资数据
        if (teachplanList.isEmpty())
        {

            for (TeachplanMedia m : dbTeachplanMediaList)
            {
                if (teachplanId.equals(m.getTeachplanId()))
                {
                    head.setTeachplanMedia(m);
                    break;
                }
            }

            return;
        }

        // 转换数据
        List<TeachplanDto> childrenList = teachplanList.stream()
                .map(t ->
                {
                    TeachplanDto teachplanDto = new TeachplanDto();
                    BeanUtils.copyProperties(t, teachplanDto);
                    return teachplanDto;
                }).collect(Collectors.toList());

        // 封装数据到子节点
        head.setTeachPlanTreeNodes(childrenList);

        // 循环每个子节点
        for (TeachplanDto children : childrenList)
        {
            // 递归调用
            buildChildrenNodes(courseId, children);
        }
    }
}
