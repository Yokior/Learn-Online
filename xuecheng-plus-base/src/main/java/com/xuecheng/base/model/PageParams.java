package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description：分页查询参数
 * @Auther：Yokior
 * @Date：2023/12/26 17:20
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageParams
{
    // 当前页码
    private Long pageNo;

    // 每页记录数
    private Long pageSize;
}
