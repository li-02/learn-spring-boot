package org.example.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoginResponse {
    private String username;
    private String accessToken;
    private LocalDate expireTime; // xxxx/xx/xx xx:xx:xx
}
