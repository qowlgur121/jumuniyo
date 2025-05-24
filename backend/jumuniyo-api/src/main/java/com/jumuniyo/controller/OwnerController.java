package com.jumuniyo.controller;

import com.jumuniyo.dto.auth.OwnerSignUpRequestDto;
import com.jumuniyo.dto.auth.OwnerSignUpResponseDto;
import com.jumuniyo.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174") // 프론트엔드 서버 주소
public class OwnerController {

    private final UserService userService;

    /**
     * 사장님 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<OwnerSignUpResponseDto> ownerSignUp(@Valid @RequestBody OwnerSignUpRequestDto requestDto) {
        try {
            OwnerSignUpResponseDto responseDto = userService.ownerSignUp(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            // 이메일 중복, 닉네임 중복 등의 경우
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 사장님 계정 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<String> getOwnerStatus() {
        // TODO: JWT 토큰에서 사용자 정보 추출하여 사장님 상태 반환
        return ResponseEntity.ok("사장님 상태 확인 기능은 구현 예정입니다.");
    }
} 