package com.xuecheng.content.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 课程基本信息(CourseBase)表实体类
 *
 * @author makejava
 * @since 2023-12-26 17:14:19
 */
@TableName(value="course_base")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseBase {
    //主键
    private Long id;
    //机构ID
    private Long companyId;
    //机构名称
    private String companyName;
    //课程名称
    private String name;
    //适用人群
    private String users;
    //课程标签
    private String tags;
    //大分类
    private String mt;
    //小分类
    private String st;
    //课程等级
    private String grade;
    //教育模式(common普通，record 录播，live直播等）
    private String teachmode;
    //课程介绍
    private String description;
    //课程图片
    private String pic;
    //创建时间
    private LocalDateTime createDate;
    //修改时间
    private LocalDateTime changeDate;
    //创建人
    private String createPeople;
    //更新人
    private String changePeople;
    //审核状态
    private String auditStatus;
    //课程发布状态 未发布  已发布 下线
    private String status;

}

