package org.example.pojo;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class User {
    Integer id;
    String username;
    String password;
    LocalDateTime lastLoginTime;
    LocalDateTime createdTime;
    LocalDateTime updatedTime;


}
