package com.amrat.JwtAuth.repository;

import com.amrat.JwtAuth.entity.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, String> {
}
