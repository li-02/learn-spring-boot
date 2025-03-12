package org.example.config;

import lombok.Data;
import org.example.aspect.EnhancedLogAspect;
import org.example.aspect.GlobalLogAspect;
import org.example.filter.RequestLoggingFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.logging")
@Data
public class LoggingAutoConfiguration {

    // 是否启用全局日志
    private boolean enabled = true;

    // 是否记录请求参数
    private boolean logRequestParams = true;

    // 是否记录响应结果
    private boolean logResponseBody = true;

    // 是否启用控制器日志
    private boolean controllerLogEnabled = true;

    // 是否启用操作日志
    private boolean operationLogEnabled = true;

    @Bean
    @ConditionalOnProperty(name = "app.logging.enabled", havingValue = "true", matchIfMissing = true)
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    @Bean
    @ConditionalOnProperty(name = "app.logging.controller-log-enabled", havingValue = "true", matchIfMissing = true)
    public GlobalLogAspect globalLogAspect() {
        return new GlobalLogAspect();
    }

    @Bean
    @ConditionalOnProperty(name = "app.logging.operation-log-enabled", havingValue = "true", matchIfMissing = true)
    public EnhancedLogAspect enhancedLogAspect() {
        return new EnhancedLogAspect();
    }

    // Getter and Setter methods
    // ...
}
