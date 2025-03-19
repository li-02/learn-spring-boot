package org.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 日志配置类
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class LogConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.logging")
    public LogProperties logProperties() {
        return new LogProperties();
    }

    /**
     * 日志配置属性
     */
    @Data
    public static class LogProperties {
        /**
         * 是否启用控制器日志（GlobalLogAspect）
         */
        private boolean controllerLogEnabled = true;

        /**
         * 是否启用操作日志（EnhancedLogAspect）
         */
        private boolean operationLogEnabled = true;

        /**
         * 最大响应数据长度
         */
        private int maxResponseLength = 10000;

        /**
         * 是否记录请求头
         */
        private boolean logRequestHeaders = false;

        /**
         * 是否记录请求体
         */
        private boolean logRequestBody = true;

        /**
         * 是否记录响应体
         */
        private boolean logResponseBody = true;
    }
}