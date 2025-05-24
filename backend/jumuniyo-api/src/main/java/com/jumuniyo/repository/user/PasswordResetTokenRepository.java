package com.jumuniyo.repository.user;

import com.jumuniyo.domain.user.PasswordResetToken;
import com.jumuniyo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 비밀번호 재설정 토큰 Repository
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * 토큰 문자열로 재설정 토큰 조회
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * 사용자로 재설정 토큰 조회 (가장 최근 생성된 것)
     */
    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);

    /**
     * 사용자의 모든 재설정 토큰 조회
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.user = :user ORDER BY t.createdAt DESC")
    Optional<PasswordResetToken> findByUser(@Param("user") User user);

    /**
     * 만료된 토큰들 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * 사용자의 기존 토큰들을 모두 사용됨으로 표시
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.used = true WHERE t.user = :user AND t.used = false")
    void markAllUserTokensAsUsed(@Param("user") User user);

    /**
     * 토큰이 존재하는지 확인
     */
    boolean existsByToken(String token);
} 