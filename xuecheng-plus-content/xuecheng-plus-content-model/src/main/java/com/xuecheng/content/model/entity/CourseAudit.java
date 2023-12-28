package com.xuecheng.content.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (CourseAudit)表实体类
 *
 * @author makejava
 * @since 2023-12-26 17:14:18
 */
@TableName(value="course_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAudit {
    
    private Long id;
    //课程id
    private Long courseId;
    //审核意见
    private String auditMind;
    //审核状态
    private String auditStatus;
    //审核人
    private String auditPeople;
    //审核时间
    private LocalDateTime auditDate;

}

