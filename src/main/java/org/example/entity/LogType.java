package org.example.entity;

import lombok.Getter;

@Getter
public enum LogType {
    OPERATION("操作日志"),
    ERROR("错误日志"),
    SECURITY("安全日志"),
    PERFORMANCE("性能日志");

    private final String description;

    LogType(String description) {
        this.description = description;
    }
}
