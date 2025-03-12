package org.example.interceptors;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.LogInfo;
import org.example.utils.LogUtil;
import org.example.utils.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 只处理Controller方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            // 获取或创建日志信息
            LogInfo logInfo = LogUtil.getLogInfo();
            logInfo.setClassName(handlerMethod.getBeanType().getName());
            logInfo.setMethodName(handlerMethod.getMethod().getName());

            // 尝试获取当前登录用户
            try {
                Map<String, Object> claims = ThreadLocalUtil.get();
                if (claims != null && claims.containsKey("username")) {
                    String username = (String) claims.get("username");
                    logInfo.setUsername(username);
                } else {
                    logInfo.setUsername("anonymous");
                }
            } catch (Exception e) {
                logInfo.setUsername("anonymous");
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        // 拦截器层面不做额外处理
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // 如果发生异常，记录异常信息
        if (ex != null) {
            LogUtil.logError("请求处理异常", ex);
        }
    }
}