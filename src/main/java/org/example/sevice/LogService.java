package org.example.sevice;

import org.example.entity.LogInfo;
import org.example.entity.LogType;

import java.time.LocalDateTime;
import java.util.List;

public interface LogService {
    /**
     * 保存日志
     */
    int saveLog(LogInfo log);

    /**
     * 记录操作日志
     */
    void recordOperationLog(String module, String operation, String requestUrl,
                            String requestMethod, String requestParams, String responseData,
                            Long executionTime, String username);

    /**
     * 记录错误日志
     */
    void recordErrorLog(String module, String operation, String requestUrl, String requestMethod, String requestParams, String exception,
                        Long executionTime, String username);

    /**
     * 记录安全日志
     */
    void recordSecurityLog(String module, String operation, String username, String ipAddress);

    /**
     * 记录性能日志
     */
    void recordPerformanceLog(String module, String operation, String methodName, Long executionTime);

    List<LogInfo> findLogsByType(LogType logType);

    /**
     * 按日期范围查询日志
     */
    List<LogInfo> findLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 按类型和日期范围查询日志
     */
    List<LogInfo> findLogsByTypeAndDateRange(LogType logType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 按用户查询日志
     */
    List<LogInfo> findLogsByUser(Long userId);

    /**
     * 按模块查询日志
     */
    List<LogInfo> findLogsByModule(String module);

    /**
     * 按请求ID查询完整请求链路
     */
    List<LogInfo> findLogsByRequestId(String requestId);
}
