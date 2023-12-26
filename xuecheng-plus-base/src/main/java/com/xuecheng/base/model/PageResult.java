package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Description：分页查询结果
 * @Auther：Yokior
 * @Date：2023/12/26 17:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> implements Serializable
{
    // 数据列表
    private List<T> items;

    // 总数
    private Long counts;

    // 当前页码
    private Long page;

    // 每页显示的条数
    private Long pageSize;
}
