package com.amrat.JwtAuth.dto;

import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    private String refreshToken;
}
