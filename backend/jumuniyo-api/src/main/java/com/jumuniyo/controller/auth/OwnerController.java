package com.jumuniyo.controller.auth;

import com.jumuniyo.dto.auth.OwnerSignUpRequestDto;
import com.jumuniyo.dto.auth.OwnerSignUpResponseDto;
import com.jumuniyo.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5174") // 프론트엔드 서버 주소
@Tag(name = "사업자 회원 API", description = "매장 사업자 회원 가입 및 관리 관련 API")
public class OwnerController {

    private final UserService userService;

    /**
     * 사장님 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "사업자 회원 가입", description = "새로운 사업자 회원 정보를 등록합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "사업자 회원 가입 요청 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = OwnerSignUpRequestDto.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "사업자 회원 가입 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OwnerSignUpResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이메일/닉네임 중복, 유효성 검사 실패)",
                         content = @Content(mediaType = "application/json", 
                                          examples = @ExampleObject(value = "{\"error\": \"이미 사용중인 이메일입니다.\"}")))
    })
    public ResponseEntity<OwnerSignUpResponseDto> ownerSignUp(@Valid @RequestBody OwnerSignUpRequestDto requestDto) {
        try {
            OwnerSignUpResponseDto responseDto = userService.ownerSignUp(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            // 이메일 중복, 닉네임 중복 등의 경우
            // 실제 애플리케이션에서는 @ControllerAdvice를 사용하여 예외를 더 체계적으로 처리하는 것이 좋습니다.
            throw new IllegalArgumentException(e.getMessage()); 
        }
    }

    /**
     * 사장님 계정 상태 확인
     */
    @GetMapping("/status")
    @Operation(summary = "사업자 계정 상태 확인 (구현 예정)", 
               description = "현재 로그인된 사업자 계정의 상태를 확인합니다. (JWT 인증 필요, 현재는 구현 예정 상태)")
    @SecurityRequirement(name = "bearerAuth") // OpenApiConfig에 정의된 SecurityScheme 이름과 일치해야 함
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 계정 상태 (문자열 응답)",
                         content = @Content(mediaType = "text/plain", 
                                          examples = @ExampleObject(value = "사장님 상태 확인 기능은 구현 예정입니다."))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰이 없거나 유효하지 않음)")
    })
    public ResponseEntity<String> getOwnerStatus() {
        // TODO: JWT 토큰에서 사용자 정보 추출하여 사장님 상태 반환
        return ResponseEntity.ok("사장님 상태 확인 기능은 구현 예정입니다.");
    }
} 