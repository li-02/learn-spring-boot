package org.example.entity;

import lombok.Data;

import java.util.Date;

@Data
public class LogInfo {
    private String requestId;     // 请求ID
    private String url;           // 请求URL
    private String httpMethod;    // HTTP方法
    private String className;     // 类名
    private String methodName;    // 方法名
    private String module;        // 模块
    private String operation;     // 操作
    private String username;      // 用户名
    private String ipAddress;     // IP地址
    private Object requestParams; // 请求参数
    private Object responseData;  // 响应数据
    private Long startTime;       // 开始时间
    private Long endTime;         // 结束时间
    private Long executionTime;   // 执行时间(ms)
    private String exception;     // 异常信息


    public LogInfo() {
        this.startTime = System.currentTimeMillis();
    }

    public void finish() {
        this.endTime = System.currentTimeMillis();
        this.executionTime = this.endTime - this.startTime;
    }

}