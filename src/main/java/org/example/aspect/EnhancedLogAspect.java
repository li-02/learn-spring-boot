package org.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.annotation.LogOperation;
import org.example.entity.LogInfo;
import org.example.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Order(1) // 优先级高于GlobalLogAspect
public class EnhancedLogAspect {

    private static final Logger log = LoggerFactory.getLogger(EnhancedLogAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 匹配所有带有@LogOperation注解的方法
    @Pointcut("@annotation(org.example.annotation.LogOperation)")
    public void logOperationPointcut() {}

    @Around("logOperationPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取注解信息
        LogOperation logOperation = method.getAnnotation(LogOperation.class);
        if (logOperation == null) {
            return point.proceed();
        }

        // 获取或创建日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        logInfo.setModule(logOperation.module());
        logInfo.setOperation(logOperation.value());

        // 是否记录请求参数
        if (logOperation.logParams()) {
            logInfo.setRequestParams(Arrays.toString(point.getArgs()));
        }

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 是否记录响应结果
            if (logOperation.logResult()) {
                logInfo.setResponseData((String) result);
            }

            // 记录日志时间信息
            logInfo.finish();

            // 简化模式下，由GlobalLogAspect负责记录日志
            // 如果没有GlobalLogAspect，则在这里记录
            if (!logInfo.getClassName().startsWith("org.example.controller")) {
                LogUtil.logInfo(logInfo);
            }

            return result;
        } catch (Throwable e) {
            // 记录异常信息
            LogUtil.logError("增强日志方法执行异常", e);
            throw e;
        }
    }
}