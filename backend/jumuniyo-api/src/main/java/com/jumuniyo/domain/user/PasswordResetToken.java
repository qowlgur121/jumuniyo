package com.jumuniyo.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 토큰 엔티티
 * 사용자가 비밀번호를 잊었을 때 이메일로 전송되는 재설정 토큰을 관리
 */
@Entity
@Table(name = "password_reset_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token; // 재설정 토큰 (UUID 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 토큰과 연결된 사용자

    @Column(nullable = false)
    private LocalDateTime expiryDate; // 토큰 만료 시간

    @Column(nullable = false)
    private boolean used = false; // 토큰 사용 여부

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 토큰 생성 시간

    @Builder
    public PasswordResetToken(String token, User user, LocalDateTime expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.used = false;
    }

    /**
     * 토큰이 만료되었는지 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 토큰이 유효한지 확인 (만료되지 않고 사용되지 않음)
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }

    /**
     * 토큰을 사용됨으로 표시
     */
    public void markAsUsed() {
        this.used = true;
    }
} 