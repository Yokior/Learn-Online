package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/5 14:48
 */
@Api(value = "课程计划编程接口", tags = "课程计划编程接口")
@RestController
@RequestMapping("/teachplan")
public class TeachplanController
{

    @Autowired
    private TeachplanService teachplanService;

    @ApiOperation("根据课程id查询课程计划树")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId)
    {
        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);

        return treeNodes;
    }

}
