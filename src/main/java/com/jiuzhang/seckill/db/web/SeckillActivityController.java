package com.jiuzhang.seckill.db.web;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.dao.SeckillCommodityDao;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.db.po.SeckillCommodity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Controller
public class SeckillActivityController {
    @Autowired  //自动生成SeckillActivityDao类到当前类里
    private SeckillActivityDao seckillActivityDao;

    //@RequestBody返回的就是"add_activity" string
    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";//找到对应名字的html,渲染过后传给client
    }

   // @ResponseBody 不是返回html模版了，返回object（return上写什么返回什么）
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(//拿到前端传来的参数
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            Map<String, Object> resultMap
    ) throws ParseException {//参数写入后端操作存入DB
        startTime = startTime.substring(0, 10) +  startTime.substring(11);
        endTime = endTime.substring(0, 10) +  endTime.substring(11);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        resultMap.put("seckillActivity", seckillActivity);
        //插入数据库
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        return "add_success";
    }

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        //找到数据库里所有activity status为1的数据放入list，return给前端
        List<SeckillActivity> seckillActivities =
                seckillActivityDao.querySeckillActivitysByStatus(1);//1：还没有结束的秒杀活动
        resultMap.put("seckillActivities", seckillActivities);
        return "seckill_activity";
    }

    @Autowired
    private SeckillCommodityDao seckillCommodityDao;
                               // {} <- Pathvariable
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long
            seckillActivityId) {
        SeckillActivity seckillActivity =
                seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity =
                seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice", seckillActivity.getOldPrice());
        resultMap.put("commodityId", seckillActivity.getCommodityId());
        resultMap.put("commodityName", seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }
}