package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.entity.LogInfo;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SysLogMapper {
    List<LogInfo> findByLogType(String logType);

    List<LogInfo> findByUserId(Long userId);

    List<LogInfo> findByModule(String module);

    List<LogInfo> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<LogInfo> findByLogTypeAndCreatedAtBetween(String logType, LocalDateTime start, LocalDateTime end);

    List<LogInfo> findByRequestId(String requestId);
}
