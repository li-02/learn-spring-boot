package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.aspect.EnhancedLogAspect;
import org.example.aspect.GlobalLogAspect;
import org.example.filter.RequestLoggingFilter;
import org.example.mapper.LogMapper;
import org.example.sevice.Impl.LogServiceImpl;
import org.example.sevice.LogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

    // 是否启用数据库日志存储
    private boolean dbLoggingEnabled = true;

    @Bean
    @ConditionalOnProperty(name = "app.logging.enabled", havingValue = "true", matchIfMissing = true)
    public RequestLoggingFilter requestLoggingFilter() {
        RequestLoggingFilter filter = new RequestLoggingFilter();
        filter.setLogRequestParams(logRequestParams);
        filter.setLogResponseBody(logResponseBody);
        return filter;
    }

    @Bean
    @ConditionalOnProperty(name = "app.logging.controller-log-enabled", havingValue = "true", matchIfMissing = true)
    public GlobalLogAspect globalLogAspect(ObjectMapper objectMapper, LogService logService) {
        return new GlobalLogAspect(objectMapper, logService);
    }

    @Bean
    @ConditionalOnProperty(name = "app.logging.operation-log-enabled", havingValue = "true", matchIfMissing = true)
    public EnhancedLogAspect enhancedLogAspect(ObjectMapper objectMapper, LogService logService) {
        return new EnhancedLogAspect(objectMapper, logService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "app.logging.db-logging-enabled", havingValue = "true", matchIfMissing = true)
    public LogService logService(LogMapper logMapper) {
        return new LogServiceImpl(logMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
