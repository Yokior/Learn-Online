package com.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.config.PayNotifyConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @Description：订单相关接口
 * @Auther：Yokior
 * @Date：2024/2/20 16:01
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService
{
    @Autowired
    private XcOrdersMapper xcOrdersMapper;

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Autowired
    private XcPayRecordMapper xcPayRecordMapper;

    @Autowired
    private OrderServiceImpl currentProxy;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    @Value("${pay.qrcodeurl}")
    private String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto)
    {
        // 添加商品订单
        XcOrders xcOrders = saveOrders(userId, addOrderDto);

        // 添加支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        Long payNo = payRecord.getPayNo();

        // 生成二维码
        String url = String.format(qrcodeurl, payNo);
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String qrCode = null;
        try
        {
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        }
        catch (IOException e)
        {
            XueChengPlusException.cast("生成二维码失败");
        }

        // 返回包含二维码
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo)
    {
        LambdaQueryWrapper<XcPayRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcPayRecord::getPayNo, payNo);

        XcPayRecord xcPayRecord = xcPayRecordMapper.selectOne(lqw);

        return xcPayRecord;
    }

    @Override
    public PayRecordDto queryPayResult(String payNo)
    {
        // 调用支付接口查询结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);

        // 更新支付记录表和订单表
        currentProxy.saveAlipayStatus(payStatusDto);

        // 返回参数
        PayRecordDto payRecordDto = new PayRecordDto();

        // 返回最新的支付记录信息
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);

        BeanUtils.copyProperties(payRecordByPayno, payRecordDto);

        return payRecordDto;
    }


    /**
     * 创建支付记录
     *
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
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());
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
     *
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
        xcOrdersGoodsList.forEach(goods ->
        {
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
     *
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

    /**
     * 请求支付宝查询结果
     *
     * @param payNo
     * @return
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo)
    {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
//bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body = null;
        try
        {
            response = alipayClient.execute(request);
            if (!response.isSuccess())
            {
                XueChengPlusException.cast("请求支付宝查询结果异常");
            }
            body = response.getBody();
        }
        catch (AlipayApiException e)
        {
            e.printStackTrace();
            XueChengPlusException.cast("请求支付宝支付结果异常");
        }

        Map bodyMap = JSON.parseObject(body, Map.class);
        Map<String,String> alipay_trade_query_response = (Map) bodyMap.get("alipay_trade_query_response");

        // 解析支付结果
        PayStatusDto payStatusDto = new PayStatusDto();

        String trade_no = alipay_trade_query_response.get("trade_no");
        String trade_status = alipay_trade_query_response.get("trade_status");
        String total_amount = alipay_trade_query_response.get("total_amount");

        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no(trade_no);
        payStatusDto.setTrade_status(trade_status);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(total_amount);

        return payStatusDto;
    }


    /**
     * 保存支付宝的支付结果
     *
     * @param payStatusDto
     */
    @Transactional
    @Override
    public void saveAlipayStatus(PayStatusDto payStatusDto)
    {
        String payNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        if (payRecordByPayno == null)
        {
            XueChengPlusException.cast("支付记录不存在");
        }

        // 拿到订单相关的id
        Long orderId = payRecordByPayno.getOrderId();
        XcOrders xcOrders = xcOrdersMapper.selectById(orderId);
        if (xcOrders == null)
        {
            XueChengPlusException.cast("订单不存在");
        }

        // 支付状态
        String statusFromDb = payRecordByPayno.getStatus();
        if ("601002".equals(statusFromDb))
        {
            // 如果已经成功
            return;
        }

        // 如果成功
        String trade_status = payStatusDto.getTrade_status();
        if ("TRADE_SUCCESS".equals(trade_status))
        {
            // 更新支付记录表的状态
            payRecordByPayno.setStatus("601002");
            // 支付宝订单号
            payRecordByPayno.setOutPayNo(payStatusDto.getTrade_status());
            // 第三方支付渠道编号
            payRecordByPayno.setOutPayChannel("Alipay");
            // 支付完成时间
            payRecordByPayno.setPaySuccessTime(LocalDateTime.now());

            xcPayRecordMapper.updateById(payRecordByPayno);

            // 更新订单状态
            xcOrders.setStatus("600002");
            xcOrdersMapper.updateById(xcOrders);

        }


    }

    @Override
    public void notifyPayResult(MqMessage message)
    {
        String jsonString = JSON.toJSONString(message);

        Message messageObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();

        // 消息id
        Long messageId = message.getId();

        CorrelationData correlationData = new CorrelationData(messageId.toString());

        correlationData.getFuture().addCallback(result -> {
            if (result.isAck())
            {
                // 消息发送交换机成功
                log.debug("发送消息成功：{}",jsonString);
                // 将消息从数据库表删除
                mqMessageService.completed(messageId);
            }
            else
            {
                // 消息发送失败
            }

        }, ex -> {
            // 发生异常
        });

        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT,"",messageObj,correlationData);
    }


}
