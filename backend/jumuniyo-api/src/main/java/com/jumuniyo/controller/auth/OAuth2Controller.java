package com.jumuniyo.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 소셜 로그인 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OAuth2 인증 API", description = "소셜 로그인 (Google, Naver, Kakao) 관련 API")
public class OAuth2Controller {

    /**
     * OAuth2 로그인 시작 엔드포인트
     * 프론트엔드에서 소셜 로그인 버튼 클릭 시 호출
     * 
     * @param provider 소셜 로그인 제공자 (google, naver, kakao)
     * @return 리다이렉트 URL
     */
    @GetMapping("/authorize/{provider}")
    @Operation(summary = "OAuth2 소셜 로그인 시작", description = "지정된 소셜 로그인 제공자(provider)의 인증 페이지로 리다이렉트합니다. 실제 인증 프로세스는 Spring Security OAuth2에 의해 처리됩니다.")
    @Parameter(name = "provider", description = "소셜 로그인 제공자 (google, naver, kakao 중 하나)", required = true, example = "google", in = ParameterIn.PATH)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "소셜 로그인 제공자의 인증 페이지로 리다이렉트")
    })
    public String redirectToOAuth2Authorization(@PathVariable String provider) {
        log.info("OAuth2 로그인 요청 - 제공자: {}", provider);
        
        // Spring Security OAuth2가 자동으로 /oauth2/authorization/{provider}로 리다이렉트하도록 처리
        // 실제로는 이 컨트롤러 메소드가 직접 호출되기보다, Spring Security Filter Chain에서 처리될 가능성이 높습니다.
        // 이 엔드포인트는 클라이언트에게 진입점을 알려주는 역할을 합니다.
        return "redirect:/oauth2/authorization/" + provider;
    }

    /**
     * OAuth2 로그인 성공 콜백 엔드포인트 (참고용)
     * 실제 처리는 OAuth2AuthenticationSuccessHandler에서 담당
     */
    @GetMapping("/callback")
    @Operation(summary = "OAuth2 로그인 콜백 (참고용)", 
               description = "소셜 로그인 성공/실패 후 호출되는 콜백 엔드포인트입니다. 실제 토큰 발급 등 주요 로직은 서버 내부의 OAuth2AuthenticationSuccessHandler에서 처리됩니다. 이 API는 클라이언트가 리다이렉션 흐름을 이해하는 데 도움을 주기 위한 참고용입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 또는 실패 상태 메시지", 
                         content = @Content(mediaType = "text/plain", 
                                          examples = {
                                              @ExampleObject(name = "성공 예시", value = "OAuth2 로그인 성공! 토큰이 발급되었습니다."),
                                              @ExampleObject(name = "실패 예시", value = "OAuth2 로그인 실패: invalid_request")
                                          }))
    })
    public String oAuth2Callback(
            @Parameter(description = "로그인 성공 시 발급된 JWT (실제로는 쿠키 또는 다른 방식으로 전달될 수 있음)", example = "eyJhbGciOiJIUzUxMiJ9...")
            @RequestParam(required = false) String token,
            @Parameter(description = "로그인 성공 여부", example = "true")
            @RequestParam(required = false) String success,
            @Parameter(description = "로그인 실패 시 에러 메시지", example = "invalid_request")
            @RequestParam(required = false) String error) {
        
        if ("true".equals(success) && token != null) {
            log.info("OAuth2 로그인 성공 콜백 수신 (참고용 API)");
            // 실제 프로덕션에서는 이 API가 직접 토큰을 보여주기보다는, 
            // 프론트엔드가 특정 페이지로 리다이렉트되거나 쿠키를 통해 토큰을 전달받는 방식이 일반적입니다.
            return "OAuth2 로그인 성공! 토큰이 발급되었습니다. (이 응답은 참고용입니다)";
        } else {
            log.error("OAuth2 로그인 실패 콜백 수신 (참고용 API): {}", error);
            return "OAuth2 로그인 실패: " + error + " (이 응답은 참고용입니다)";
        }
    }

    /**
     * 지원하는 OAuth2 제공자 목록 조회
     */
    @GetMapping("/providers")
    @Operation(summary = "지원하는 OAuth2 제공자 목록 조회", description = "현재 시스템에서 지원하는 소셜 로그인 제공자(provider) 목록을 문자열 배열로 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지원하는 제공자 목록", 
                         content = @Content(mediaType = "application/json", 
                                          schema = @Schema(type = "array", implementation = String.class),
                                          examples = @ExampleObject(value = "[\"google\", \"naver\", \"kakao\"]")))
    })
    public String[] getSupportedProviders() {
        return new String[]{"google", "naver", "kakao"};
    }
} 