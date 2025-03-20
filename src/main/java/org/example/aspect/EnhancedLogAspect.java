package org.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.annotation.LogOperation;
import org.example.entity.LogInfo;
import org.example.entity.LogType;
import org.example.sevice.LogService;
import org.example.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Order(1) // 优先级高于GlobalLogAspect
public class EnhancedLogAspect {

    private static final Logger log = LoggerFactory.getLogger(EnhancedLogAspect.class);
    private final ObjectMapper objectMapper;
    private final LogService logService;

    @Value("${app.logging.operation-log-enabled:true}")
    private boolean operationLogEnabled;

    // 匹配所有带有@LogOperation注解的方法
    @Pointcut("@annotation(org.example.annotation.LogOperation)")
    public void logOperationPointcut() {}

    @Around("logOperationPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!operationLogEnabled) {
            return point.proceed();
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取注解信息
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        if (logOperation == null) {
            return point.proceed();
        }

        // 获取或创建日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        enrichLogInfo(point, method, logOperation, logInfo);

        try {
            // 执行目标方法
            Object result = point.proceed();
            // 记录响应结果
            if (logOperation.logResult()) {
                recordResponseData(result, logInfo);
            }
            // 完成日志记录（但不保存，由过滤器同意保存）
            logInfo.finish();
            // 如果这是一个单独的操作（不是在请求上下文中），则立即保存日志
            if (shouldSaveImmediately(point)) {
                LogUtil.logInfo(logInfo);
                logService.saveLog(logInfo);
                LogUtil.markAsCommitted();
            }
            return result;
        } catch (Throwable e) {
            // 记录异常信息
            logInfo.setLogType(LogType.ERROR.name());
            logInfo.setException(e.getMessage());
            logInfo.finish();
            if (shouldSaveImmediately(point)) {
                LogUtil.logError("增强日志方法执行异常", e);
                logService.saveLog(logInfo);
                LogUtil.markAsCommitted();
            }
            throw e;
        }
    }

    /**
     * 是否应该立即保存日志
     *
     * @param point
     * @return
     */
    private boolean shouldSaveImmediately(ProceedingJoinPoint point) {
        // 如果是在定时任务、消息监听器等非Web请求上下文中执行的方法，则立即保存
        // 这里使用简单的判断：如果类不在controller、service包下，可能是独立操作
        String className = point.getTarget().getClass().getName();
        return !className.startsWith("org.example.controller.") && !className.startsWith("org.example.service.");
    }

    /**
     * 丰富日志信息
     *
     * @param point
     * @param method
     * @param logOperation
     * @param logInfo
     */
    private void enrichLogInfo(ProceedingJoinPoint point, Method method, LogOperation logOperation, LogInfo logInfo) {
        logInfo.setModule(logOperation.module());
        logInfo.setOperation(logOperation.value() + " [Enhanced]");
        logInfo.setClassName(point.getTarget().getClass().getName());
        logInfo.setMethodName(method.getName());
        if (logOperation.logParams()) {
            try {
                logInfo.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
            } catch (Exception e) {
                logInfo.setRequestParams(Arrays.toString(point.getArgs()));
            }
        }
    }

    private void recordResponseData(Object result, LogInfo logInfo) {
        if (result != null) {
            try {
                String responseStr = objectMapper.writeValueAsString(result);
                if (responseStr.length() > 10000) {
                    responseStr = responseStr.substring(0, 10000) + "...(truncated)";
                }
                logInfo.setResponseData(responseStr);
            } catch (Exception e) {
                logInfo.setResponseData("Failed to serialize response" + e.getMessage());
            }
        }
    }

}