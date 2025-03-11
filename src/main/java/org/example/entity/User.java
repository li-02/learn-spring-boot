package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class User {
    Integer id;
    String username;
    @JsonIgnore // 让springmvc序列化时忽略这个字段
    String password;
    LocalDateTime lastLoginTime;
    LocalDateTime createdTime; //驼峰命名和数据库中的下划线命名不一致，所以需要额外配置yml文件，才能正确映射
    LocalDateTime updatedTime;
}
