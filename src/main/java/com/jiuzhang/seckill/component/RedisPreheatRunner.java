package com.jiuzhang.seckill.component;

import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.util.RedisService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class RedisPreheatRunner implements ApplicationRunner {
    @Resource
    RedisService redisService;
    @Resource
    SeckillActivityDao seckillActivityDao;
    /**
     * 启动项目时 把所有数据从DB读到 Redis 里存入 商品库存 * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SeckillActivity> seckillActivities =
                seckillActivityDao.querySeckillActivitysByStatus(1);
        for (SeckillActivity seckillActivity : seckillActivities) {
            redisService.setValue("stock:" + seckillActivity.getId(), (long) seckillActivity.getAvailableStock());
        }
    }
}
