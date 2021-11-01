package com.jiuzhang.seckill;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication
@MapperScan("com.jiuzhang.seckill.db.mappers")
@ComponentScan(basePackages = {"com.jiuzhang"}) //springboot 和 mybatis整合时扫描的文件
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }
}