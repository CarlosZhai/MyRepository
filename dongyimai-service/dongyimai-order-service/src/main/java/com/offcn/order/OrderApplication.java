package com.offcn.order;

import com.offcn.config.FeignInterceptor;
import com.offcn.utils.IdWorker;
import com.offcn.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = "com.offcn.order.dao")
@EnableFeignClients(basePackages = {"com.offcn.sellergoods.feign","com.offcn.user.feign"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public FeignInterceptor getFeignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    public TokenDecode getTokenDecode() {
        return new TokenDecode();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }
}
