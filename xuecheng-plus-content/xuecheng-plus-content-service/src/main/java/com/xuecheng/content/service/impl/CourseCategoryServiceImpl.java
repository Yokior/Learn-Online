package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/30 17:03
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService
{

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    // 全部分类
    private List<CourseCategory> totalCategoryList;

    /**
     * 查询课程分类节点
     *
     * @return
     */
    @Override
    public CourseCategoryTreeDto queryTreeNodes()
    {
        CourseCategoryTreeDto treeDto = new CourseCategoryTreeDto();
        totalCategoryList = courseCategoryMapper.selectList(null);
        // 查询自身的子节点
        buildChildrenNodes("1",treeDto);

        return treeDto;
    }

    /**
     * 根据父节点Id查找子节点
     * @param id 自身Id 也就是父节点Id
     * @param head 指针头
     * @return
     */
    private void buildChildrenNodes(String id, CourseCategoryTreeDto head)
    {
        // 根据父节点的Id查询子节点
        List<CourseCategory> courseCategoryList = totalCategoryList.stream()
                .filter(t -> t.getParentid().equals(id))
                .collect(Collectors.toList());

        // 没有子节点 返回null
        if (courseCategoryList.isEmpty())
        {
            return;
        }

        // 转换数据
        List<CourseCategoryTreeDto> childrenList = courseCategoryList.stream()
                .map(c ->
                {
                    CourseCategoryTreeDto dto = new CourseCategoryTreeDto();
                    BeanUtils.copyProperties(c, dto);
                    return dto;
                }).collect(Collectors.toList());

        // 封装数据到子节点
        head.setChildrenTreeNodes(childrenList);

        // 循环每个子节点
        for (CourseCategoryTreeDto children : childrenList)
        {
            // 递归调用
            buildChildrenNodes(children.getId(), children);
        }
    }
}
