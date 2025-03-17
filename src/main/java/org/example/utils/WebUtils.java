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
}
