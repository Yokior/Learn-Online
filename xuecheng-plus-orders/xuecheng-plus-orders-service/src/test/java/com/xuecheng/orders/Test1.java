package com.xuecheng.orders;

import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class Test1
{

    @Autowired
    private XcOrdersGoodsMapper xcOrdersGoodsMapper;

    @Test
    public void test()
    {
        XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
        xcOrdersGoods.setId(1234L);
        xcOrdersGoods.setGoodsId("456");
        xcOrdersGoods.setOrderId(1L);
        xcOrdersGoods.setGoodsName("dasd");
        xcOrdersGoods.setGoodsPrice(456.5F);

        XcOrdersGoods xcOrdersGoods2 = new XcOrdersGoods();
        xcOrdersGoods2.setId(123L);
        xcOrdersGoods2.setGoodsId("46");
        xcOrdersGoods2.setOrderId(5L);
        xcOrdersGoods2.setGoodsName("dasjj");
        xcOrdersGoods2.setGoodsPrice(454.5F);

        List<XcOrdersGoods> goodsList = new ArrayList<>();
        goodsList.add(xcOrdersGoods);
        goodsList.add(xcOrdersGoods2);


        int i = xcOrdersGoodsMapper.insertBatch(goodsList);
        if (i <= 0)
        {
            System.out.println("插入失败");
        }
        else
        {
            System.out.println("插入成功");
        }

    }

}
