package com.yunlan.config;

import com.yunlan.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    private static final String[] EXCLUDE_PATHS = {
            "/customer/open/**",
            "/publics/**",
            "/foundations/**",
            "/areas/**",
            "/verifyCodes/**",
            "/logins/**",
            "/login/**",
            "/address/**",
            "/market/consumer/activity/list",
            "/customer/consumer/evaluation/pageByTarget",
            "/customer/consumer/evaluation/countEvaluationByServeItemId",
            "/doc.html",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**",
            "/favicon.ico"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}
