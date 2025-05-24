package com.jumuniyo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비밀번호 변경 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "토큰을 입력해주세요.")
    private String token;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String confirmPassword;
} 