package com.amrat.JwtAuth.service;


import com.amrat.JwtAuth.dto.*;
import com.amrat.JwtAuth.entity.RefreshToken;
import com.amrat.JwtAuth.entity.RevokedAccessToken;
import com.amrat.JwtAuth.entity.User;
import com.amrat.JwtAuth.repository.RefreshTokenRepository;
import com.amrat.JwtAuth.repository.RevokedAccessTokenRepository;
import com.amrat.JwtAuth.util.CurrentUser;
import com.amrat.JwtAuth.util.JwtUtil;
import com.amrat.JwtAuth.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RevokedAccessTokenRepository revokedAccessTokenRepository;
    private final JwtUtil jwtUtil;
    private final CurrentUser currentUser;

    public RegistrationResponseDto register(RegistrationRequestDto registrationRequestDto) {
        // register user
        User user = userService.registerUser(registrationRequestDto);
        // return user dto
        return modelMapper.map(user, RegistrationResponseDto.class);
    }

    // user login
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        System.out.println("Login service");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword()));
        System.out.println("below authentication.");

        User user = Optional.ofNullable(authentication.getPrincipal())
                .filter(u -> u instanceof User)
                .map(u -> (User) u)
                .orElseThrow(() -> new BadCredentialsException("User Principle is missing."));

        // generate refresh token
        String refreshToken = TokenHashUtil.generateRawToken();
        String hashedRefreshToken = TokenHashUtil.hashToken(refreshToken);
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        RefreshToken token = new RefreshToken(user, hashedRefreshToken, expiredAt);
        refreshTokenRepository.save(token);

        // generate access token
        String accessToken = jwtUtil.generateAccessToken(user);

        return new LoginResponseDto(accessToken, refreshToken);

    }

    // refresh token
    public RefreshTokenResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequestDto) {
        // hash raw expired token
        String hashedRawToken = TokenHashUtil.hashToken(refreshTokenRequestDto.getRefreshToken());

        // check does refresh token exists or not
        RefreshToken stored = refreshTokenRepository.findByRefreshToken(hashedRawToken).orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        System.out.println(stored.getId());
        // check if refresh token already used or not
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(stored.getUser());
            System.out.println("All tokens revoked.");
            throw new RuntimeException("Token reuse detected. All sessions have been revoked.");
        }

        // check expiration
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        // revoke token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // issue new refresh token
        String newRawToken = TokenHashUtil.generateRawToken();
        String newHashedToken = TokenHashUtil.hashToken(newRawToken);
        RefreshToken newRefreshToken = new RefreshToken(stored.getUser(), newHashedToken, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(newRefreshToken);

        // generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(stored.getUser());

        return new RefreshTokenResponseDto(newAccessToken, newRawToken);
    }

    // user logout
    public void logout(String authorizationHeader) {
        User user = currentUser.getCurrentUser();

        if (user == null) {
            throw new RuntimeException("Login first.");
        }

        String accessToken = extractAccessToken(authorizationHeader);
        String jti = jwtUtil.getJtiFromToken(accessToken);
        LocalDateTime expiresAt = jwtUtil.getExpirationFromToken(accessToken);

        if (jti == null || jti.isBlank()) {
            throw new RuntimeException("Access token id is missing.");
        }

        revokedAccessTokenRepository.save(new RevokedAccessToken(jti, expiresAt));
        refreshTokenRepository.revokeAllByUser(user);
        SecurityContextHolder.clearContext();
    }

    private String extractAccessToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Bearer access token is required.");
        }

        return authorizationHeader.substring(7);
    }

}
