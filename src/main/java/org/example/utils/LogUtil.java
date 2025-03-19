package org.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.LogInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * 日志工具类，用于管理日志上下文
 */
public class LogUtil {
    private static final Logger log = LoggerFactory.getLogger(LogUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ThreadLocal<LogInfo> logInfoThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> logCommitedThreadLocal = new ThreadLocal<>();

    /**
     * 为当前线程初始化日志上下文
     *
     * @return 新创建的日志信息对象
     */
    public static LogInfo initLogContext() {
        LogInfo currentThreadLogInfo = new LogInfo();
        currentThreadLogInfo.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        logInfoThreadLocal.set(currentThreadLogInfo);
        // 设置日志未提交表示
        logCommitedThreadLocal.set(false);
        log.debug("初始化日志上下文: {}", currentThreadLogInfo.getRequestId());
        return currentThreadLogInfo;
    }

    /**
     * 获取当前线程的日志信息
     *
     * @return 当前线程的日志信息，如果没有就创建新的
     */
    public static LogInfo getLogInfo() {
        LogInfo logInfo = logInfoThreadLocal.get();
        if (logInfo == null) {
            logInfo = initLogContext();
        }
        return logInfo;
    }

    /**
     * 标记当前现成的日志为已记录状态
     */
    public static void markAsCommitted() {
        logCommitedThreadLocal.set(true);
    }

    /**
     * 检查当前线程的日志是否记录
     *
     * @return 是否已记录
     */
    public static boolean isCommitted() {
        Boolean committed = logCommitedThreadLocal.get();
        return committed != null && committed;
    }

    /**
     * 清理当前线程的日志上下文
     */
    public static void clearLogContext() {
        log.debug("清理日志上下文");
        logInfoThreadLocal.remove();
        logCommitedThreadLocal.remove();
    }

    /**
     * 获取当前请求
     *
     * @return 请求
     */
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

    /**
     * 记录普通信息
     *
     * @param logInfo 日志信息
     */
    public static void logInfo(LogInfo logInfo) {
        if (logInfo != null) {
            log.info("操作日志: type={}, module={}, operation={}, time={}ms",
                    logInfo.getLogType(), logInfo.getModule(), logInfo.getOperation(), logInfo.getExecutionTime());
        }
    }

    /**
     * 记录错误信息
     *
     * @param message 错误信息
     * @param ex      异常
     */
    public static void logError(String message, Throwable ex) {
        LogInfo logInfo = logInfoThreadLocal.get();
        if (logInfo != null) {
            log.error("错误日志: requestId={}, module={}, operation={}, message={}",
                    logInfo.getRequestId(), logInfo.getModule(), logInfo.getOperation(), message, ex);
        } else {
            log.error(message, ex);
        }
    }
}