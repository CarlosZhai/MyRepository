package com.offcn.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * 资源授权配置
 */
@Configuration
@EnableResourceServer //开启资源校验(校验令牌)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)//激活方法上的@PreAuthorize注解
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    //定义公钥
    private static final String PUBLIC_KEY = "public.key";

    /**
     * 定义JwtTokenStore
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    /**
     * 定义JJwtAccessTokenConverter
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getPubKey());
        return converter;
    }

    /**
     * 获取非对称加密公钥 Key
     *
     * @return
     */
    private String getPubKey() {
        ClassPathResource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Http安全配置，对每个到达系统的http请求链接进行校验
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //所有请求都需要认证通过
        http.authorizeRequests()
                .antMatchers("/user/add","/user/load/*") //配置放行的地址
                .permitAll()
                .anyRequest()
                .authenticated(); //其他地址需要认证授权
    }
}
