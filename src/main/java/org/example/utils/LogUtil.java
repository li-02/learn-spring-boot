package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogUtil {
    private static final Logger log = LoggerFactory.getLogger(LogUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ThreadLocal<LogInfo> logInfoThreadLocal = new ThreadLocal<>();

    // 获取当前线程的日志信息
    public static LogInfo getLogInfo() {
        LogInfo logInfo = logInfoThreadLocal.get();
        if (logInfo == null) {
            logInfo = new LogInfo();
            logInfo.setRequestId(UUID.randomUUID().toString().replace("-", ""));
            logInfoThreadLocal.set(logInfo);
        }
        return logInfo;
    }

    // 清除当前线程的日志信息
    public static void clearLogInfo() {
        logInfoThreadLocal.remove();
    }

    // 获取当前请求
    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // 获取客户端IP
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","));
        }

        return ip;
    }

    // 记录日志信息
    public static void logInfo(LogInfo logInfo) {
        try {
            // 创建一个不包含LocalDateTime的简单Map
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("requestId", logInfo.getRequestId());
            logMap.put("logType", logInfo.getLogType());
            logMap.put("requestUrl", logInfo.getRequestUrl());
            logMap.put("requestMethod", logInfo.getRequestMethod());
            logMap.put("module", logInfo.getModule());
            logMap.put("operation", logInfo.getOperation());
            logMap.put("className", logInfo.getClassName());
            logMap.put("methodName", logInfo.getMethodName());
            logMap.put("requestParams", logInfo.getRequestParams());
            logMap.put("responseData", logInfo.getResponseData());
            logMap.put("responseCode", logInfo.getResponseCode());
            logMap.put("userId", logInfo.getUserId());
            logMap.put("username", logInfo.getUsername());
            logMap.put("userAgent", logInfo.getUserAgent());
            logMap.put("ipAddress", logInfo.getIpAddress());
            logMap.put("startTime", logInfo.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logMap.put("endTime", logInfo.getEndTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            logMap.put("executionTime", logInfo.getExecutionTime());
            logMap.put("exception", logInfo.getException());
            logMap.put("deviceInfo", logInfo.getDeviceInfo());
            if (logInfo.getCreatedAt() != null) {
                logMap.put("createdAt", logInfo.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            log.info("【操作日志】{}", objectMapper.writeValueAsString(logMap));
        } catch (Exception e) {
            log.error("日志序列化失败", e);
        }
    }

    // 记录异常日志
    public static void logError(String message, Throwable ex) {
        LogInfo logInfo = getLogInfo();
        logInfo.setException(ex.getMessage());
        logInfo.finish();

        log.error("【错误日志】{}", message, ex);
        logInfo(logInfo);
    }
}