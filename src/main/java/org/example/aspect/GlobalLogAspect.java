package org.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.entity.LogInfo;
import org.example.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Order(2)
public class GlobalLogAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalLogAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 匹配所有Controller包下的所有方法
    @Pointcut("execution(* org.example.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return point.proceed();
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取或创建日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        logInfo.setClassName(point.getTarget().getClass().getName());
        logInfo.setMethodName(method.getName());

        // 记录方法参数
        logInfo.setRequestParams(Arrays.toString(point.getArgs()));

        try {
            // 执行目标方法
            Object result = point.proceed();

            // 记录响应结果
            logInfo.setResponseData((String) result);
            logInfo.finish();

            // 记录日志
            LogUtil.logInfo(logInfo);

            return result;
        } catch (Throwable e) {
            // 记录异常信息
            LogUtil.logError("方法执行异常", e);
            throw e;
        }
    }
}
