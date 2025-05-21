package com.jumuniyo.controller.user; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.dto.user.UserSignUpRequestDto;
import com.jumuniyo.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스가 RESTful API의 컨트롤러임을 나타냄. @Controller + @ResponseBody
// @RestController는 RESTful API를 쉽게 만들 수 있도록 스프링이 제공하는 편리한 기능들을 활성화해준다
@RequestMapping("/api/v1/auth") // 이 컨트롤러의 모든 API는 "/api/v1/auth" 경로를 기본으로 가짐
@RequiredArgsConstructor // final 필드 또는 @NonNull 필드에 대한 생성자를 자동으로 생성 (의존성 주입)
public class UserController {

    private final UserService userService;

    // 회원가입 API 엔드포인트
    // HTTP POST 요청을 "/api/v1/auth/signup" 경로로 받음
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserSignUpRequestDto requestDto) {
        // @Valid: requestDto 객체에 대해 유효성 검사 수행 (DTO에 정의된 @NotBlank, @Email 등)
        //         유효성 검사 실패 시 MethodArgumentNotValidException 발생 (추후 전역 예외 처리 필요)
        // @RequestBody: HTTP 요청 본문(JSON 등)을 UserSignUpRequestDto 객체로 변환

        userService.signUp(requestDto); // 서비스 계층의 회원가입 로직 호출

        // 회원가입 성공 시 HTTP 201 Created 상태 코드와 함께 성공 메시지 반환
        // ResponseEntity를 사용하면 HTTP 상태 코드, 헤더, 본문을 직접 제어할 수 있음
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
        // 또는, 간단히 성공 여부만 알리고 싶다면:
        // return ResponseEntity.status(HttpStatus.CREATED).build(); // 본문 없이 상태 코드만 반환
        // 또는, 생성된 사용자 정보를 응답으로 보내고 싶다면 UserResponseDto를 반환할 수도 있음
        // UserResponseDto responseDto = userService.signUpAndGetUser(requestDto); // 서비스 메소드가 UserResponseDto 반환 가정
        // return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // TODO: 추후 로그인 API 엔드포인트 (@PostMapping("/login")) 추가 예정
    // TODO: 추후 회원정보 조회/수정/탈퇴 API 엔드포인트 추가 예정
}