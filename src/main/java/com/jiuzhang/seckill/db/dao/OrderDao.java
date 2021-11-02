package com.jiuzhang.seckill.db.dao;

import com.jiuzhang.seckill.db.po.SeckillOrder;

public interface OrderDao {
    void insertOrder(SeckillOrder order);

    SeckillOrder queryOrder(String orderNo);

    void updateOrder(SeckillOrder order);

}
