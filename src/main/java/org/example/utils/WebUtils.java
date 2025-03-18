package org.example.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class WebUtils {

    /**
     * 获取当前请求对象
     */
    public static Optional<HttpServletRequest> getRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(attributes -> attributes instanceof ServletRequestAttributes)
                .map(attributes -> ((ServletRequestAttributes) attributes).getRequest());
    }

    /**
     * 获取客户端IP
     *
     * @return ip
     */
    public static String getClientIp() {
        return getRequest().map(request -> {
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
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }).orElse("unknown");
    }

    /**
     * 获取用户代理
     *
     * @return 用户代理
     */
    public static String getUserAgent() {
        return getRequest()
                .map(request -> request.getHeader("User-Agent"))
                .orElse(null);
    }

    /**
     * 获取设备信息
     */
    public static String getDeviceInfo() {
        return getUserAgent(); // 简单实现，实际可以解析User-Agent获取更详细的设备信息
    }

    /**
     * 判断是否是AJAX请求
     */
    public static boolean isAjaxRequest() {
        return getRequest()
                .map(request -> "XMLHttpRequest".equals(request.getHeader("X-Requested-With")))
                .orElse(false);
    }

}
