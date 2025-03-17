package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.LogInfo;
import org.example.utils.LogUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

// order 最高级别：会在其他过滤器之前执行

/**
 * OncePerRequestFileter是Spring提供的一个抽象类，用于确保每个请求只被过滤一次
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    /**
     * 实现过滤器的具体逻辑
     *
     * @param request
     * @param response
     * @param filterChain 表示过滤器链，用于将请求传递给下一个过滤器或最终的Servlet
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 如果是资源文件，则跳过日志记录
        if (isAssetRequest(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 包装请求和响应，以便多次读取
        // 默认情况下，HttpServletRequest 和 HttpServletResponse 的输入流（InputStream）和输出流（OutputStream）只能被读取一次。
        // 如果你需要多次读取请求体或响应体（例如记录日志或验证数据），直接操作原始对象是不可行的
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // 初始化日志信息
        LogInfo logInfo = LogUtil.getLogInfo();
        logInfo.setRequestUrl(request.getRequestURI());
        logInfo.setRequestMethod(request.getMethod());
        logInfo.setIpAddress(LogUtil.getClientIp());

        try {
            // 执行请求
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 仅当没有更详细的日志记录时，记录基本HTTP请求信息
            if (logInfo.getClassName() == null) {
                logInfo.finish();
                LogUtil.logInfo(logInfo);
            }

            // 清理线程本地变量
            LogUtil.clearLogInfo();

            // 复制响应内容（重要步骤！） 确保相应内容被写入到响应中
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean isAssetRequest(String uri) {
        return uri.matches(".+\\.(css|js|html|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$");
    }
}