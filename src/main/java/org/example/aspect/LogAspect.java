package org.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.annotation.LogOperation;
import org.example.utils.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日志切面类，用于记录操作日志
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切入点：标记了@LogOperation注解的所有方法
     */
    @Pointcut("@annotation(org.example.annotation.LogOperation)")
    public void logPointcut() {
    }

    /**
     * 在方法执行前后记录日志
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 获取请求的各种信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 执行方法并捕获可能的异常
        Object result = null;
        Exception exception = null;
        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 执行时长(毫秒)
            long time = System.currentTimeMillis() - beginTime;

            // 记录日志
            recordLog(point, time, result, exception, attributes);
        }
    }

    /**
     * 记录日志的详细方法
     */
    private void recordLog(JoinPoint joinPoint, long time, Object result, Exception exception,
                           ServletRequestAttributes attributes) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            LogOperation logOperation = method.getAnnotation(LogOperation.class);
            if (logOperation == null) {
                return;
            }

            // 收集日志信息到一个Map，便于以后扩展到数据库存储
            Map<String, Object> logInfo = new HashMap<>();

            // 1. 基本操作信息
            logInfo.put("operationType", logOperation.value());
            logInfo.put("module", logOperation.module());
            logInfo.put("method", joinPoint.getTarget().getClass().getName() + "." + signature.getName());
            logInfo.put("executionTime", time);

            // 2. 请求参数
            Object[] args = joinPoint.getArgs();
            String argsStr = formatArgs(args);
            logInfo.put("params", argsStr);

            // 3. 返回结果
            if (result != null) {
                try {
                    // 对于大对象或敏感数据，可能需要限制结果大小或脱敏处理
                    String resultStr = objectMapper.writeValueAsString(result);
                    if (resultStr.length() > 1000) {
                        resultStr = resultStr.substring(0, 1000) + "... [内容过长已省略]";
                    }
                    logInfo.put("result", resultStr);
                } catch (Exception e) {
                    logInfo.put("result", "结果序列化失败: " + e.getMessage());
                }
            }

            // 4. 异常信息
            if (exception != null) {
                logInfo.put("status", "失败");
                logInfo.put("exception", exception.getClass().getName());
                logInfo.put("errorMessage", exception.getMessage());
            } else {
                logInfo.put("status", "成功");
            }

            // 5. 用户信息 - 从ThreadLocal获取
            String username = "未登录";
            // 从ThreadLocal获取用户信息
            Map<String, Object> userInfo = ThreadLocalUtil.get();
            if (userInfo != null && userInfo.containsKey("username")) {
                username = String.valueOf(userInfo.get("username"));
            }
            logInfo.put("username", username);

            // 用户ID
            if (userInfo != null && userInfo.containsKey("id")) {
                logInfo.put("userId", userInfo.get("id"));
            }

            // 6. 请求信息
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                logInfo.put("url", request.getRequestURL().toString());
                logInfo.put("httpMethod", request.getMethod());
                logInfo.put("ip", getClientIp(request));
                logInfo.put("userAgent", request.getHeader("User-Agent"));

                // 记录客户端提供的令牌信息 (可选，用于跟踪和调试)
                logInfo.put("token", request.getHeader("Authorization"));
            }

            // 7. 时间戳
            logInfo.put("timestamp", System.currentTimeMillis());

            // 输出日志信息
            logger.info("【操作日志】{}", objectMapper.writeValueAsString(logInfo));

            // 保存日志到数据库
//            logService.saveLog(logInfo);
        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
        }
    }

    /**
     * 格式化方法参数
     */
    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }

                    try {
                        // 简单类型直接转字符串
                        if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
                            return arg.toString();
                        }

                        // 复杂对象尝试用Jackson序列化
                        return objectMapper.writeValueAsString(arg);
                    } catch (Exception e) {
                        return arg.getClass().getSimpleName() + "@" + Integer.toHexString(arg.hashCode());
                    }
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}