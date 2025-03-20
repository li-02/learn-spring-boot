package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.LogInfo;
import org.example.entity.LogType;
import org.example.sevice.LogService;
import org.example.utils.LogUtil;
import org.example.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;

// order 最高级别：会在其他过滤器之前执行

/**
 * OncePerRequestFileter是Spring提供的一个抽象类，用于确保每个请求只被过滤一次
 * 日志过滤器，用于初始化和保存请求日志
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Data
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final LogService logService;
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
        LogInfo logInfo = LogUtil.initLogContext();
        logInfo.setLogType(LogType.OPERATION.name());
        logInfo.setCreatedAt(LocalDateTime.now());
        // 记录请求信息
        logInfo.setRequestUrl(requestWrapper.getRequestURL().toString());
        logInfo.setRequestMethod(requestWrapper.getMethod());
        // 添加IP地址和用户代理信息
        logInfo.setIpAddress(WebUtils.getClientIp());
        logInfo.setUserAgent(requestWrapper.getHeader("User-Agent"));

        try {
            // 继续执行过滤器链
            filterChain.doFilter(requestWrapper, responseWrapper);
            // 记录响应码
            logInfo.setResponseCode(String.valueOf(responseWrapper.getStatus()));
        } finally {
            // 复制响应内容（重要步骤！） 确保相应内容被写入到响应中
            responseWrapper.copyBodyToResponse();

            // 请求结束时，如果日志未被提交，则保存默认日志
            if (!LogUtil.isCommitted()) {
                logInfo.finish();
                tryToSaveLog(logInfo);
            }
            // 清理日志上下文
            LogUtil.clearLogContext();
        }
    }

    private void tryToSaveLog(LogInfo logInfo) {
        try {
            // 记录到日志文件
            LogUtil.logInfo(logInfo);
            // 保存到数据库
            logService.saveLog(logInfo);
            // 标记为已提交
            LogUtil.markAsCommitted();
        } catch (Exception e) {
            log.error("保存日志到数据库失败", e);
        }
    }

    private boolean isAssetRequest(String uri) {
        return uri.matches(".+\\.(css|js|html|jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$");
    }
}