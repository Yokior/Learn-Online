package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/4 14:02
 */
@Data
@ApiModel(value = "EditCourseDto", description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto
{
    @NotNull(message = "课程id不能为空")
    @ApiModelProperty(value = "课程ID", required = true)
    private Long courseId;
}
