package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.LogInfo;
import org.example.utils.LogUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// order 最高级别：会在其他过滤器之前执行

/**
 * OncePerRequestFileter是Spring提供的一个抽象类，用于确保每个请求只被过滤一次
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Data
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    private boolean logRequestParams = true;
    private boolean logResponseBody = true;
    private int maxPayloadLength = 10000;

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

        // 如果是文件上传或者二进制内容，跳过记录
        if (request.getContentType() != null &&
                (request.getContentType().contains("multipart/form-data") ||
                        request.getContentType().contains("application/octet-stream"))) {
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
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 执行请求
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            stopWatch.stop();
            // 请求完成后，记录请求和响应信息
            if (logRequestParams && requestWrapper.getContentAsByteArray().length > 0) {
                String requestBody = new String(requestWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (requestBody.length() > maxPayloadLength) {
                    requestBody = requestBody.substring(0, maxPayloadLength) + "...(truncated)";
                }
                logInfo.setRequestParams(requestBody);
            }
            if (logResponseBody && responseWrapper.getContentAsByteArray().length > 0) {
                String requestBody = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (requestBody.length() > maxPayloadLength) {
                    requestBody = requestBody.substring(0, maxPayloadLength) + "...(truncated)";
                }
                logInfo.setResponseData(requestBody);
            }
            // 复制响应内容（重要步骤！） 确保相应内容被写入到响应中
            responseWrapper.copyBodyToResponse();

            // 这里我们不记录日志，因为后续的AOP切面会处理，除非没有匹配的切面
            // 没有后续切面处理（比如静态资源请求），就在这里记录日志
            String uri = request.getRequestURI();
            if (isAssetRequest(uri)) {
                if (log.isDebugEnabled()) {
                    log.debug("Static resource request: {}", uri);
                }
                LogUtil.clearLogInfo();
            }
        }
    }

    private boolean isAssetRequest(String uri) {
        return uri.matches(".+\\.(css|js|html|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$");
    }
}