package org.example.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class LogInfo {
    private Long id;
    private String logType; // 日志类型
    private String requestId;     // 请求ID
    private String requestUrl;    // 请求URL
    private String requestMethod;    // HTTP方法
    private String module;        // 模块
    private String operation;     // 操作描述
    private String className;     // 类名
    private String methodName;    // 方法名
    private String requestParams; // 请求参数
    private String responseData;  // 响应数据
    private String responseCode; // 响应码
    private Long userId;    // 用户id
    private String username;      // 用户名
    private String userAgent;   // 用户代理
    private String ipAddress;     // IP地址
    private LocalDateTime startTime;       // 开始时间
    private LocalDateTime endTime;         // 结束时间
    private Long executionTime;   // 执行时间(ms)
    private String exception;     // 异常信息
    private String deviceInfo;  // 用户设备信息
    private LocalDateTime createdAt;    // 创建时间

    // 修改finish方法
    public void finish() {
        this.endTime = LocalDateTime.now();
        this.executionTime = ChronoUnit.MILLIS.between(this.startTime, this.endTime);
    }

    // 构造函数
    public LogInfo() {
        this.startTime = LocalDateTime.now();
    }

}