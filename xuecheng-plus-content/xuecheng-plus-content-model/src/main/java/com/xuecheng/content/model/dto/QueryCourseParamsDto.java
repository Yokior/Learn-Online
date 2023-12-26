package com.xuecheng.content.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description：课程查询条件模型类
 * @Auther：Yokior
 * @Date：2023/12/26 17:24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryCourseParamsDto
{
    // 审核状态
    private String auditStatus;

    // 课程名称
    private String courseName;

    // 发布状态
    private String publishStatus;


}
