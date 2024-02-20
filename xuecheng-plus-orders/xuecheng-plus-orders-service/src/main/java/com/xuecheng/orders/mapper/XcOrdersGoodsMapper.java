package com.xuecheng.orders.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface XcOrdersGoodsMapper extends BaseMapper<XcOrdersGoods>
{
    int insertBatch(List<XcOrdersGoods> list);
}
