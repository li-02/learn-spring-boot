package org.example.sevice.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.LogInfo;
import org.example.entity.LogType;
import org.example.mapper.LogMapper;
import org.example.sevice.LogService;
import org.example.utils.WebUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    @Override
    public int saveLog(LogInfo logInfo) {
        try {
            if (logInfo.getCreatedAt() == null) {
                logInfo.setCreatedAt(LocalDateTime.now());
            }
            return logMapper.insert(logInfo);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void recordOperationLog(String module, String operation, String requestUrl, String requestMethod,
                                   String requestParams, String responseData, Long executionTime, String username) {
        LogInfo logInfo = new LogInfo();
        logInfo.setLogType(LogType.OPERATION.name());
        logInfo.setModule(module);
        logInfo.setOperation(operation);
        logInfo.setRequestId(generateRequestId());
        logInfo.setUsername(username);
        logInfo.setIpAddress(WebUtils.getClientIp());
        logInfo.setUserAgent(WebUtils.getUserAgent());
        logInfo.setRequestUrl(requestUrl);
        logInfo.setRequestMethod(requestMethod);
        logInfo.setRequestParams(requestParams);
        logInfo.setResponseData(responseData);
        logInfo.setExecutionTime(executionTime);
        logInfo.setClassName(getCallerClassName());
        logInfo.setMethodName(getCallerMethodName());
        logInfo.setCreatedAt(LocalDateTime.now());
        saveLog(logInfo);
    }

    @Override
    public void recordErrorLog(String module, String operation, String requestUrl, String requestMethod,
                               String requestParams, String exception, Long executionTime, String username) {
        LogInfo logInfo = new LogInfo();
        logInfo.setLogType(LogType.ERROR.name());
        logInfo.setModule(module);
        logInfo.setOperation(operation);
        logInfo.setRequestId(generateRequestId());
        logInfo.setUsername(username);
        logInfo.setIpAddress(WebUtils.getClientIp());
        logInfo.setRequestUrl(requestUrl);
        logInfo.setRequestMethod(requestMethod);
        logInfo.setRequestParams(requestParams);
        logInfo.setException(exception);
        logInfo.setExecutionTime(executionTime);
        logInfo.setClassName(getCallerClassName());
        logInfo.setMethodName(getCallerMethodName());
        logInfo.setCreatedAt(LocalDateTime.now());
        saveLog(logInfo);
    }

    @Override
    public void recordSecurityLog(String module, String operation, String username, String ipAddress) {
        LogInfo logInfo = new LogInfo();
        logInfo.setLogType(LogType.SECURITY.name());
        logInfo.setModule(module);
        logInfo.setOperation(operation);
        logInfo.setRequestId(generateRequestId());
        logInfo.setUsername(username);
        logInfo.setIpAddress(ipAddress);
        logInfo.setUserAgent(WebUtils.getUserAgent());
        logInfo.setDeviceInfo(WebUtils.getDeviceInfo());
        logInfo.setCreatedAt(LocalDateTime.now());
        saveLog(logInfo);
    }

    @Override
    public void recordPerformanceLog(String module, String operation, String methodName, Long executionTime) {
        LogInfo logInfo = new LogInfo();
        logInfo.setLogType(LogType.PERFORMANCE.name());
        logInfo.setModule(module);
        logInfo.setOperation(operation);
        logInfo.setRequestId(generateRequestId());
        logInfo.setMethodName(methodName);
        logInfo.setExecutionTime(executionTime);
        logInfo.setClassName(getCallerClassName());
        logInfo.setCreatedAt(LocalDateTime.now());
        saveLog(logInfo);
    }

    @Override
    public List<LogInfo> findLogsByType(LogType logType) {
        return logMapper.selectByLogType(logType.name());
    }

    @Override
    public List<LogInfo> findLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return logMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public List<LogInfo> findLogsByTypeAndDateRange(LogType logType, LocalDateTime startDate, LocalDateTime endDate) {
        return logMapper.selectByTypeAndDateRange(logType.name(), startDate, endDate);
    }

    @Override
    public List<LogInfo> findLogsByUser(Long userId) {
        return logMapper.selectByUserId(userId);
    }

    @Override
    public List<LogInfo> findLogsByModule(String module) {
        return logMapper.selectByModule(module);
    }

    @Override
    public List<LogInfo> findLogsByRequestId(String requestId) {
        return logMapper.selectByRequestId(requestId);
    }

    // Helper methods
    private String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String getCallerClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            return stackTrace[3].getClassName();
        }
        return null;
    }

    private String getCallerMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            return stackTrace[3].getMethodName();
        }
        return null;
    }
}
