package com.xuecheng.content.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/27 13:43
 */
@Data
@ApiModel(value = "BindTeachplanMediaDto", description = "教学计划-媒资绑定提交数据")
public class BindTeachplanMediaDto
{
    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @JsonProperty("fileName")
    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String mediaFilename;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;
}
