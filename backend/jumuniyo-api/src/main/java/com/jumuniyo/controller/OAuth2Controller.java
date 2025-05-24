package com.jumuniyo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 소셜 로그인 관련 API를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/oauth2")
public class OAuth2Controller {

    /**
     * OAuth2 로그인 시작 엔드포인트
     * 프론트엔드에서 소셜 로그인 버튼 클릭 시 호출
     * 
     * @param provider 소셜 로그인 제공자 (google, naver, kakao)
     * @return 리다이렉트 URL
     */
    @GetMapping("/authorize/{provider}")
    public String redirectToOAuth2Authorization(@PathVariable String provider) {
        log.info("OAuth2 로그인 요청 - 제공자: {}", provider);
        
        // Spring Security OAuth2가 자동으로 /oauth2/authorization/{provider}로 리다이렉트하도록 처리
        return "redirect:/oauth2/authorization/" + provider;
    }

    /**
     * OAuth2 로그인 성공 콜백 엔드포인트 (참고용)
     * 실제 처리는 OAuth2AuthenticationSuccessHandler에서 담당
     */
    @GetMapping("/callback")
    public String oAuth2Callback(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error) {
        
        if ("true".equals(success) && token != null) {
            log.info("OAuth2 로그인 성공 콜백 수신");
            return "OAuth2 로그인 성공! 토큰이 발급되었습니다.";
        } else {
            log.error("OAuth2 로그인 실패 콜백 수신: {}", error);
            return "OAuth2 로그인 실패: " + error;
        }
    }

    /**
     * 지원하는 OAuth2 제공자 목록 조회
     */
    @GetMapping("/providers")
    public String[] getSupportedProviders() {
        return new String[]{"google", "naver", "kakao"};
    }
} 