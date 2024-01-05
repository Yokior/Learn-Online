package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.entity.Teachplan;
import com.xuecheng.content.model.entity.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/5 14:44
 */
@Data
public class TeachplanDto extends Teachplan
{
    // 与媒资管理的信息
    private TeachplanMedia teachplanMedia;

    // 小章节列表
    private List<TeachplanDto> teachPlanTreeNodes;
}
