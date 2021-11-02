package com.jiuzhang.seckill.db.dao;

import com.jiuzhang.seckill.db.mappers.SeckillOrderMapper;
import com.jiuzhang.seckill.db.po.SeckillOrder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class OrderDaoImpl implements OrderDao {
    @Resource
    private SeckillOrderMapper orderMapper;
    @Override
    public void insertOrder(SeckillOrder order) {
        orderMapper.insert(order);
    }
    @Override
    public SeckillOrder queryOrder(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }
    @Override
    public void updateOrder(SeckillOrder order) {
        orderMapper.updateByPrimaryKey(order);
    } }