package com.jumuniyo.dto.user;

import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginResponseDto {
    
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
    private String nickname;
    private UserRole role;
    private UserStatus status;
    private String profileImageUrl;

    public static UserLoginResponseDto of(String token, User user) {
        return UserLoginResponseDto.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .status(user.getStatus())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
} 