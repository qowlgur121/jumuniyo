package com.jumuniyo.dto.auth;

import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OwnerSignUpResponseDto {
    
    private Long userId;
    private String email;
    private String nickname;
    private String phoneNumber;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private String message;
    
    public static OwnerSignUpResponseDto of(Long userId, String email, String nickname, String phoneNumber, 
                                           UserRole role, UserStatus status, LocalDateTime createdAt) {
        return OwnerSignUpResponseDto.builder()
                .userId(userId)
                .email(email)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .role(role)
                .status(status)
                .createdAt(createdAt)
                .message("사장님 계정이 성공적으로 생성되었습니다. 승인 후 서비스를 이용하실 수 있습니다.")
                .build();
    }
} 