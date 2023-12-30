package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.entity.CourseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2023/12/30 16:53
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCategoryTreeDto extends CourseCategory
{
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
