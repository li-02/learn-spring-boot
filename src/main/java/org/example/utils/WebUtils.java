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
     * 判断是否是AJAX请求
     */
    public static boolean isAjaxRequest() {
        return getRequest()
                .map(request -> "XMLHttpRequest".equals(request.getHeader("X-Requested-With")))
                .orElse(false);
    }

    /**
     * 获取设备信息
     *
     * @return 设备信息字符串
     */
    public static String getDeviceInfo() {
        HttpServletRequest request = LogUtil.getRequest();
        if (request == null) {
            return "unknown";
        }

        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "unknown";
        }

        StringBuilder deviceInfo = new StringBuilder();

        // 检测操作系统
        if (userAgent.contains("Windows")) {
            deviceInfo.append("Windows");
        } else if (userAgent.contains("Mac")) {
            deviceInfo.append("MacOS");
        } else if (userAgent.contains("Linux")) {
            deviceInfo.append("Linux");
        } else if (userAgent.contains("Android")) {
            deviceInfo.append("Android");
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            deviceInfo.append("iOS");
        } else {
            deviceInfo.append("Other OS");
        }

        deviceInfo.append(" | ");

        // 检测浏览器
        if (userAgent.contains("Chrome") && !userAgent.contains("Edg")) {
            deviceInfo.append("Chrome");
        } else if (userAgent.contains("Firefox")) {
            deviceInfo.append("Firefox");
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) {
            deviceInfo.append("Safari");
        } else if (userAgent.contains("Edg")) {
            deviceInfo.append("Edge");
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            deviceInfo.append("Internet Explorer");
        } else {
            deviceInfo.append("Other Browser");
        }

        return deviceInfo.toString();
    }

}
