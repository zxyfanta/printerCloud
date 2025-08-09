package com.printercloud.config;

import com.printercloud.security.JwtAuthInterceptor;
import com.printercloud.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${jwt.secret:printercloud-secret}")
    private String jwtSecret;

    @Value("${jwt.expires:86400}")
    private long jwtExpires;

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil(jwtSecret, jwtExpires);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtAuthInterceptor(jwtUtil()))
                .addPathPatterns("/api/**");
    }
}

