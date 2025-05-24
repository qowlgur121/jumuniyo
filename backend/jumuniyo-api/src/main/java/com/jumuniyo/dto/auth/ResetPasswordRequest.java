package com.jumuniyo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 재설정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;
} 