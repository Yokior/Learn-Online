package com.xuecheng.media.model.dto;

import lombok.Data;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/16 20:09
 */
@Data
public class UploadFileParamsDto
{
    private String filename;

    private String fileType;

    private Long fileSize;

    private String tags;

    private String username;

    private String remark;
}
