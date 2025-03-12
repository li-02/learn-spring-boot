package org.example.config;

import org.example.interceptors.LoggingInterceptor;
import org.example.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    /**
     * 这里的注册顺序就是拦截器的执行顺序
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(loginInterceptor).excludePathPatterns("/auth/login",
                "/auth/register");
        registry.addInterceptor(loggingInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/error/**");
    }
}
