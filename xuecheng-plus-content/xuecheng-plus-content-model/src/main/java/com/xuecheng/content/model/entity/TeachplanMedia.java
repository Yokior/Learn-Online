package com.xuecheng.content.model.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (TeachplanMedia)表实体类
 *
 * @author makejava
 * @since 2023-12-26 17:14:20
 */
@TableName(value="teachplan_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachplanMedia {
    //主键
    private Long id;
    //媒资文件id
    private String mediaId;
    //课程计划标识
    private Long teachplanId;
    //课程标识
    private Long courseId;
    //媒资文件原始名称
    private String mediaFilename;
    
    private LocalDateTime createDate;
    //创建人
    private String createPeople;
    //修改人
    private String changePeople;

}

