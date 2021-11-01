package com.jiuzhang.seckill.service;

import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Service
public class SeckillOverSellService {
    @Autowired
    private SeckillActivityDao seckillActivityDao;//注入Dao对象

    //库存扣减方法
    public String  processSeckill(long activityId) {
        SeckillActivity seckillActivity =
                seckillActivityDao.querySeckillActivityById(activityId);
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        if (availableStock > 0) {
            result = "恭喜，抢购成功";
            System.out.println(result);
            availableStock = availableStock - 1;
            seckillActivity.setAvailableStock(new Integer("" + availableStock));
            seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
            result = "抱歉，抢购失败，商品被抢完了";
            System.out.println(result);
        }
        return result;
    }

}
