package com.jumuniyo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 이메일 찾기 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class FindEmailRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phoneNumber;
} 