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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class GlobalLogAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalLogAspect.class);
    private final ObjectMapper objectMapper;
    private final LogService logService;

    @Value("${app.logging.controller-log-enabled:true}")
    private boolean controllerLogEnabled;

    // 匹配所有Controller包下的所有方法
    @Pointcut("execution(* org.example.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!controllerLogEnabled) {
            return point.proceed();
        }
        // 如果方法已有@LogOperation注解,则由EnhancedLogAspect处理
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        if (method.isAnnotationPresent(LogOperation.class)) {
            return point.proceed();
        }
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }


        // 获取或创建日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        enrichLogInfo(point, method, logInfo);
        try {
            // 执行目标方法
            Object result = point.proceed();
            // 记录响应结果
            recordResponseData(result, logInfo);
            logInfo.finish();
            return result;
        } catch (Exception e) {
            // 记录异常信息
            logInfo.setLogType(LogType.ERROR.name());
            logInfo.setException(e.getMessage());
            logInfo.finish();
            throw e;
        }
    }


    private void enrichLogInfo(ProceedingJoinPoint point, Method method, LogInfo logInfo) {
        logInfo.setClassName(point.getTarget().getClass().getName());
        logInfo.setMethodName(method.getName());

        // 推断模块和操作
        String className = point.getTarget().getClass().getName();
        String controllerName = className.substring(className.lastIndexOf(".") + 1).replace("Controller", "");
        logInfo.setModule(controllerName);
        logInfo.setOperation(method.getName() + " [Global]");
        // 记录方法参数
        try {
            logInfo.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
        } catch (Exception e) {
            logInfo.setRequestParams(Arrays.toString(point.getArgs()));
        }

    }

    /**
     * 记录响应数据
     */
    private void recordResponseData(Object result, LogInfo logInfo) {
        if (result != null) {
            try {
                String responseStr = objectMapper.writeValueAsString(result);
                // 如果响应数据太大，可以截断
                if (responseStr.length() > 10000) {
                    responseStr = responseStr.substring(0, 10000) + "...(truncated)";
                }
                logInfo.setResponseData(responseStr);
            } catch (Exception e) {
                logInfo.setResponseData("Failed to serialize response " + e.getMessage());
            }
        }
    }
}
