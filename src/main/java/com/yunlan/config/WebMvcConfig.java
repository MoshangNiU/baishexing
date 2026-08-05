package com.yunlan.config;

import com.yunlan.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Value("${upload.path:./uploads}")
    private String uploadPath;

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
            "/pay/notify/**",
            "/admin-api/auth/login",
            "/uploads/**",
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new File(uploadPath).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }
}
