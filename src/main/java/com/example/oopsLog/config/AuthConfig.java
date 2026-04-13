package com.example.oopsLog.config;

import com.example.oopsLog.common.auth.SessionAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new SessionAuthInterceptor())
//                .addPathPatterns("/api/**")
//                .excludePathPatterns(
//                        "/api/users/signup",
//                        "/api/users/login",
//                        "/api/users/logout",
//                        "/error",
//                        "/favicon.ico"
//                );
    }
}