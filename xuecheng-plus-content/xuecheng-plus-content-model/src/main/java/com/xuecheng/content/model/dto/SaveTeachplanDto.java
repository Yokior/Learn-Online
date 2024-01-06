package com.xuecheng.content.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Description：新增大章节 小章节 修改章节
 * @Auther：Yokior
 * @Date：2024/1/6 14:50
 */
@Data
public class SaveTeachplanDto
{
    // 教学计划id
    private Long id;

    // 课程计划名称
    @NotEmpty(message = "课程计划名称不能为空")
    private String pname;

    // 课程计划父id
    @NotNull(message = "课程计划父id不能为空")
    private Long parentid;

    // 层级
    @NotNull(message = "层级不能为空")
    private Integer grade;

    // 课程类型： 1视频 2文档
    private String mediaType;

    // 课程标识
    @NotNull(message = "课程标识不能为空")
    private Long courseId;

    // 课程发布标识
    private Long coursePubId;

    // 是否支持试看
    private String isPreview;

}
