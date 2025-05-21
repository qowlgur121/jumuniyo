package com.jumuniyo.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"), // 자발적 탈퇴 또는 휴면
    SUSPENDED("정지"), // 관리자에 의한 이용 정지
    PENDING_EMAIL_VERIFICATION("이메일 인증 대기"); // 이메일 인증 필요 상태

    private final String description;
}