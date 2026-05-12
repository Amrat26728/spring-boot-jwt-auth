package com.amrat.JwtAuth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegistrationResponseDto {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
