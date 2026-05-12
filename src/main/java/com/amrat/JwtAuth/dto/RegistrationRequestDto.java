package com.amrat.JwtAuth.dto;

import lombok.Data;

@Data
public class RegistrationRequestDto {
    private String fullName;
    private String username;
    private String password;
}
