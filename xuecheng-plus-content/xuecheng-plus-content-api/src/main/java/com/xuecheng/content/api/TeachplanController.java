package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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


    @ApiOperation("课程计划创建或修改")
    @PostMapping
    public void saveTeachplan(@RequestBody @Validated SaveTeachplanDto saveTeachplanDto)
    {
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation(value = "课程计划和媒资信息绑定")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto)
    {
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }

}
