package org.example.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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
import java.time.LocalDateTime;
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

        HttpServletRequest request = attributes.getRequest();


        // 获取或创建日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        logInfo.setLogType(LogType.OPERATION.name());
        logInfo.setClassName(point.getTarget().getClass().getName());
        logInfo.setMethodName(method.getName());
        logInfo.setCreatedAt(LocalDateTime.now());

        // 推断模块和操作
        String className = point.getTarget().getClass().getName();
        String controllerName = className.substring(className.lastIndexOf(".") + 1).replace("Controller", "");
        logInfo.setModule(controllerName);
        logInfo.setOperation(method.getName());
        // 记录方法参数
        try {
            logInfo.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
        } catch (Exception e) {
            logInfo.setRequestParams(Arrays.toString(point.getArgs()));
        }

        try {
            // 执行目标方法
            Object result = point.proceed();
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
            // 记录日志时间信息
            logInfo.finish();

            // 记录日志
            LogUtil.logInfo(logInfo);

            // 保存到数据库
            try {
                logService.saveLog(logInfo);
            } catch (Exception e) {
                log.error("保存日志到数据库失败", e);
            }

            return result;
        } catch (Throwable e) {
            // 记录异常信息
            logInfo.setLogType(LogType.ERROR.name());
            logInfo.setException(e.getMessage());
            logInfo.finish();

            // 记录到日志文件
            LogUtil.logError("方法执行异常", e);

            // 保存到数据库
            try {
                logService.saveLog(logInfo);
            } catch (Exception ex) {
                log.error("保存日志到数据库失败", ex);
            }

            throw e;
        } finally {
            LogUtil.clearLogInfo();
        }
    }
}
