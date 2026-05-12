package com.amrat.JwtAuth.repository;

import com.amrat.JwtAuth.entity.RefreshToken;
import com.amrat.JwtAuth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String hashedRawToken);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true WHERE t.user = :user")
    void revokeAllByUser(User user);
}
