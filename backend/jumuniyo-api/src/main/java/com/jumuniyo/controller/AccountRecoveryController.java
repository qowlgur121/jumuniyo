package com.jumuniyo.controller;

import com.jumuniyo.dto.auth.*;
import com.jumuniyo.service.auth.AccountRecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 계정 복구 관련 API 컨트롤러 (이메일 찾기, 비밀번호 재설정)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AccountRecoveryController {

    private final AccountRecoveryService accountRecoveryService;

    /**
     * 이메일 찾기
     */
    @PostMapping("/find-email")
    public ResponseEntity<FindEmailResponse> findEmail(@Valid @RequestBody FindEmailRequest request) {
        log.info("이메일 찾기 API 호출 - 닉네임: {}", request.getNickname());
        
        FindEmailResponse response = accountRecoveryService.findEmail(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 재설정 요청 (이메일 전송)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("비밀번호 재설정 요청 API 호출 - 이메일: {}", request.getEmail());
        
        accountRecoveryService.requestPasswordReset(request);
        
        // 보안상 이유로 항상 성공 메시지 반환
        return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 전송되었습니다.");
    }

    /**
     * 비밀번호 재설정 토큰 유효성 검증
     */
    @GetMapping("/reset-password/validate")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
        log.info("비밀번호 재설정 토큰 검증 API 호출");
        
        boolean isValid = accountRecoveryService.validateResetToken(token);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 비밀번호 변경
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("비밀번호 변경 API 호출");
        
        boolean success = accountRecoveryService.changePassword(request);
        
        if (success) {
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("비밀번호 변경에 실패했습니다. 토큰을 확인해주세요.");
        }
    }
} 