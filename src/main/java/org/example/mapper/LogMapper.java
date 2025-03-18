package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.LogInfo;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LogMapper {

    int insert(LogInfo logInfo);

    /**
     * 按日志类型查询
     */
    List<LogInfo> selectByLogType(String logType);

    /**
     * 按日期范围查询
     */
    List<LogInfo> selectByDateRange(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 按类型和日期范围查询
     */
    List<LogInfo> selectByTypeAndDateRange(@Param("logType") String logType,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 按用户ID查询
     */
    List<LogInfo> selectByUserId(Long userId);

    /**
     * 按模块查询
     */
    List<LogInfo> selectByModule(String module);

    /**
     * 按请求ID查询
     */
    List<LogInfo> selectByRequestId(String requestId);
}
