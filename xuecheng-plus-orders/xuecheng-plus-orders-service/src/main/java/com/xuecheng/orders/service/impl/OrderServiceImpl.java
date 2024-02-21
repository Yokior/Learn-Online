package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description：订单相关接口
 * @Auther：Yokior
 * @Date：2024/2/20 16:01
 */
@Service
public class OrderServiceImpl implements OrderService
{
    @Autowired
    private XcOrdersMapper xcOrdersMapper;

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    private XcPayRecordMapper xcPayRecordMapper;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto)
    {
        // 添加商品订单
        XcOrders xcOrders = saveOrders(userId, addOrderDto);

        // 添加支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);

        // 生成二维码


        return null;
    }


    /**
     * 创建支付记录
     * @param xcOrders
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders xcOrders)
    {
        // 订单id
        Long ordersId = xcOrders.getId();
        XcOrders dbXcOrders = xcOrdersMapper.selectById(ordersId);
        if (dbXcOrders == null)
        {
            XueChengPlusException.cast("订单不存在");
        }

        // 订单状态
        String status = dbXcOrders.getStatus();
        // 如果订单支付结果为成功 不再添加支付记录 避免重复支付
        if ("600002".equals(status))
        {
            XueChengPlusException.cast("订单已支付");
        }

        // 添加支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setId(IdWorkerUtils.getInstance().nextId());
        xcPayRecord.setOrderId(ordersId);
        xcPayRecord.setOrderName(dbXcOrders.getOrderName());
        xcPayRecord.setTotalPrice(dbXcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setStatus("601001"); // 未支付
        xcPayRecord.setUserId(dbXcOrders.getUserId());

        int insert = xcPayRecordMapper.insert(xcPayRecord);
        if (insert <= 0)
        {
            XueChengPlusException.cast("插入支付记录失败");
        }

        return xcPayRecord;
    }



    /**
     * 保存订单信息
     * @param userId
     * @param addOrderDto
     * @return
     */
    public XcOrders saveOrders(String userId, AddOrderDto addOrderDto)
    {
        // 幂等性判断 同一个选课记录只能有一个订单
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (xcOrders != null)
        {
            return xcOrders;
        }

        // 插入订单主表
        xcOrders = new XcOrders();
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setStatus("600001"); // 未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201"); // 订单类型 购买课程
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId()); // 记录选课表的id

        int insert = xcOrdersMapper.insert(xcOrders);
        if (insert <= 0)
        {
            XueChengPlusException.cast("添加订单信息失败");
        }

        // 插入订单明细表
        Long orderId = xcOrders.getId();

        // 将前端传入的明细json转换List
        String orderDetail = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetail, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            goods.setOrderId(orderId);
        });

        int insertBatch = xcOrdersGoodsMapper.insertBatch(xcOrdersGoodsList);
        if (insertBatch <= 0)
        {
            XueChengPlusException.cast("添加订单明细失败");
        }

        return xcOrders;
    }


    /**
     * 根据业务id查询订单 业务id是选课记录表中的主键
     * @param businessId
     * @return
     */
    public XcOrders getOrderByBusinessId(String businessId)
    {
        LambdaQueryWrapper<XcOrders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcOrders::getOutBusinessId, businessId);
        XcOrders xcOrders = xcOrdersMapper.selectOne(lqw);

        return xcOrders;
    }


}
