package com.xuecheng.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @Description：订单服务
 * @Auther：Yokior
 * @Date：2024/2/20 16:01
 */
public interface OrderService
{
    /**
     * 创建商品订单
     * @param userId
     * @param addOrderDto
     * @return
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * 查询支付记录
     * @param payNo
     * @return
     */
    XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo
     * @return
     */
    PayRecordDto queryPayResult(String payNo);

    /**
     * 保存订单状态
     * @param payStatusDto
     */
    void saveAlipayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     * @param message
     */
    void notifyPayResult(MqMessage message);
}
