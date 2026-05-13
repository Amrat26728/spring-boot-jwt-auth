package com.amrat.JwtAuth.controller;

import com.amrat.JwtAuth.dto.UserDetailsDto;
import com.amrat.JwtAuth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDto> getUserDetails(@PathVariable String id) {
        return ResponseEntity.ok().body(userService.getUserById(id));
    }

}
