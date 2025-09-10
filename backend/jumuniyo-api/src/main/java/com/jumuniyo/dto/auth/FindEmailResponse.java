package com.jumuniyo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이메일 찾기 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FindEmailResponse {

    private String maskedEmail; // 마스킹된 이메일 (예: abc***@gmail.com)
    private String message;

    public static FindEmailResponse success(String maskedEmail) {
        return new FindEmailResponse(maskedEmail, "이메일을 찾았습니다.");
    }

    public static FindEmailResponse notFound() {
        return new FindEmailResponse(null, "입력하신 정보와 일치하는 계정을 찾을 수 없습니다.");
    }
} 