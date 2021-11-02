package com.jiuzhang.seckill;

import com.jiuzhang.seckill.mq.RocketMQService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.util.Date;

@SpringBootTest
public class MQTest {
    @Resource
    RocketMQService rocketMQService;
    @Test
    public void sendMQTest() throws Exception {
        rocketMQService.sendMessage("test-jiuzhang", "Hello World!" +
                new Date().toString());
    }
}