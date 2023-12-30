package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.entity.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/30 16:56
 */
@Api(value = "课程分类管理", tags = "课程分类管理")
@RestController
@RequestMapping("/course-category")
public class CourseCategoryController
{

    @Autowired
    private CourseCategoryService courseCategoryService;

    @ApiOperation("分类根节点查询")
    @GetMapping("/tree-nodes")
    public CourseCategoryTreeDto queryTreeNodes()
    {
        CourseCategoryTreeDto courseCategoryTreeDto = courseCategoryService.queryTreeNodes();

        return courseCategoryTreeDto;
    }
}
