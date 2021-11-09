package com.jiuzhang.seckill.web;
import com.jiuzhang.seckill.db.dao.OrderDao;
import com.jiuzhang.seckill.db.dao.SeckillActivityDao;
import com.jiuzhang.seckill.db.dao.SeckillCommodityDao;
import com.jiuzhang.seckill.db.po.SeckillActivity;
import com.jiuzhang.seckill.db.po.SeckillCommodity;
import com.jiuzhang.seckill.db.po.SeckillOrder;
import com.jiuzhang.seckill.service.SeckillActivityService;
import com.jiuzhang.seckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {
    @Autowired  //自动生成SeckillActivityDao类到当前类里
    private SeckillActivityDao seckillActivityDao;
    @Autowired
    OrderDao orderDao;
    @Autowired
    private SeckillCommodityDao seckillCommodityDao;
    @Resource
    private RedisService redisService;

    /* 添加秒杀活动
     */
    //@RequestBody返回的就是"add_activity" string
    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";//找到对应名字的html,渲染过后传给client
    }

    /*
    添加秒杀活动后将数据传入后端
     */
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
        //插入数据库
        seckillActivityDao.inertSeckillActivity(seckillActivity);
        resultMap.put("seckillActivity", seckillActivity);
        return "add_success";
    }

    /*
    全部秒杀活动展示
     */
    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        //找到数据库里所有activity status为1的数据放入list，return给前端
        List<SeckillActivity> seckillActivities =
                seckillActivityDao.querySeckillActivitysByStatus(1);//1：还没有结束的秒杀活动
        resultMap.put("seckillActivities", seckillActivities);
        return "seckill_activity";
    }

    /*
      展示秒杀活动页面
     */                      // {} <- Pathvariable
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) { ////匹配上面的{}，将参数放入long中
        SeckillActivity seckillActivity =
                seckillActivityDao.querySeckillActivityById(seckillActivityId);//通过id，把activity从数据库查出来
        SeckillCommodity seckillCommodity =
                seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());//把库存从数据库查出来
        //把这些显示在页面上，以下两种二选一
        resultMap.put("seckillActivity", seckillActivity);
        resultMap.put("seckillCommodity", seckillCommodity);
//        resultMap.put("seckillPrice", seckillActivity.getSeckillPrice());
//        resultMap.put("oldPrice", seckillActivity.getOldPrice());
//        resultMap.put("commodityId", seckillActivity.getCommodityId());
//        resultMap.put("commodityName", seckillCommodity.getCommodityName());//库存有的，否则NPE
//        resultMap.put("commodityDesc", seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    /**
     * 处理抢购请求
     * @param userId
     * @param seckillActivityId * @return
     */
    @Autowired
    SeckillActivityService seckillActivityService;

    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(@PathVariable long userId,
                                         @PathVariable long seckillActivityId) {
        boolean stockValidateResult = false;
        ModelAndView modelAndView = new ModelAndView();
        try {
            /*
             * 判断用户是否在已购名单中
             */
            if (redisService.isInLimitMember(seckillActivityId, userId)) {
                //提示用户已经在限购名单中，返回结果
                modelAndView.addObject("resultInfo", "对不起，您已经在限购名单中");
                modelAndView.setViewName("seckill_result");
                return modelAndView;
            }
            /*
             * 确认是否能够进行秒杀 */
            stockValidateResult =
                    seckillActivityService.seckillStockValidator(seckillActivityId);
            if (stockValidateResult) {
                SeckillOrder order =
                        seckillActivityService.createOrder(seckillActivityId, userId); modelAndView.addObject("resultInfo","秒杀成功，订单创建中，订单ID:"
                        + order.getOrderNo());
                modelAndView.addObject("orderNo",order.getOrderNo());
                //添加用户到已购名单中
                redisService.addLimitMember(seckillActivityId, userId);
            } else { modelAndView.addObject("resultInfo","对不起，商品库存不足");
            }
        } catch (Exception e) {
            log.error("秒杀系统异常" + e.toString());
            modelAndView.addObject("resultInfo","秒杀失败"); }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    /**
     * 订单查询
     * @param orderNo * @return
     */
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(@PathVariable String orderNo) {
        log.info("订单查询，订单号:" + orderNo);
        SeckillOrder order = orderDao.queryOrder(orderNo); ModelAndView modelAndView = new ModelAndView();
        if (order != null) {
            modelAndView.setViewName("order");//放入指定调转页面
            modelAndView.addObject("order", order);//放入对象
            SeckillActivity seckillActivity =
                    seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            modelAndView.setViewName("wait");
        }
        return modelAndView;
    }
    /**
     * 订单支付 * @return */
    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(@PathVariable String orderNo) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }
}