package org.example.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.exception.CustomerException;
import org.example.utils.JwtUtil;
import org.example.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,  HttpServletResponse response,  Object handler) throws Exception {
        // 令牌验证
        String token=request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new CustomerException("401", "您无权限操作");
        }
        // 从redis中获取token
        String redisToken = stringRedisTemplate.opsForValue().get(token);
        if (redisToken == null) {
            throw new CustomerException("401", "您无权限操作");
        }
        Map<String, Object> claims;
        try{
            claims = JwtUtil.parseToken(token);
        } catch (Exception e) {
            throw new CustomerException("401", "您无权限操作");
        }
        // 将用户信息存入ThreadLocal
        ThreadLocalUtil.set(claims);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清空ThreadLocal
        ThreadLocalUtil.remove();
    }
}
