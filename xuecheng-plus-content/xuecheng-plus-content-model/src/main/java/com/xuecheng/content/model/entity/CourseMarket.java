package com.xuecheng.content.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 课程营销信息(CourseMarket)表实体类
 *
 * @author makejava
 * @since 2023-12-26 17:14:20
 */
@TableName(value="course_market")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseMarket {
    //主键，课程id
    private Long id;
    //收费规则，对应数据字典
    private String charge;
    //现价
    private Object price;
    //原价
    private Object originalPrice;
    //咨询qq
    private String qq;
    //微信
    private String wechat;
    //电话
    private String phone;
    //有效期天数
    private Integer validDays;

}

