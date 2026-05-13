package com.amrat.JwtAuth.dto;

import lombok.Data;

@Data
public class UserDetailsDto {
    private String id;
    private String fullName;
    private String username;
}
