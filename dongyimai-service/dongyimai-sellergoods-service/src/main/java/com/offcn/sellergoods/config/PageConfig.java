package com.offcn.sellergoods.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PageConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new
                PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        //设置请求的⻚⾯⼤于最⼤⻚后操作，true调回到⾸⻚，false继续请求 默认false
        paginationInnerInterceptor.setOverflow(true);
        //设置最⼤单⻚限制数量，默认 500 条， -1 不受限制
        paginationInnerInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }
}
