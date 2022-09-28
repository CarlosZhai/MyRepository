package com.offcn.sellergoods;

import com.offcn.config.FeignInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.offcn.sellergoods.dao")
public class SellerGoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SellerGoodsApplication.class);
    }

    /***
     * 创建拦截器对象
     */
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}