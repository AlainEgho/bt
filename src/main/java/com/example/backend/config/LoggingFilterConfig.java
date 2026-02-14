package com.example.backend.config;

import com.example.backend.logging.ApiLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class LoggingFilterConfig {

    @Bean
    public FilterRegistrationBean<ApiLoggingFilter> apiLoggingFilterRegistration(ApiLoggingFilter filter) {
        FilterRegistrationBean<ApiLoggingFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return reg;
    }
}
