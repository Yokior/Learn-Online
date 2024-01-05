package com.xuecheng.content.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
     * @param courseId
     * @return
     */
    @Override
    public List<TeachplanDto> getTreeNodes(Long courseId)
    {
        this.dbTeachplanList = teachplanMapper.selectList(null);
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
     * 根据课程计划id构造子节点树
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
