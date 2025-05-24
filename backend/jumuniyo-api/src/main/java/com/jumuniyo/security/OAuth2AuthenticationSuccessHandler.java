package com.jumuniyo.security;

import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.OAuth2UserInfo;
import com.jumuniyo.dto.OAuth2UserInfoFactory;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import com.jumuniyo.repository.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

/**
 * OAuth2 인증 성공 후 처리를 담당하는 핸들러
 * JWT 토큰을 발급하고 프론트엔드로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 프론트엔드 리다이렉트 URL (나중에 설정으로 분리 가능)
    private static final String REDIRECT_URL = "http://localhost:3000/auth/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String registrationId = getRegistrationId(request);
            
            log.info("OAuth2 인증 성공 - 제공자: {}", registrationId);

            // OAuth2 사용자 정보 추출
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
            
            // 사용자 조회 또는 생성
            User user = getOrCreateUser(userInfo);
            
            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
            
            // 성공 페이지로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String redirectUrl = UriComponentsBuilder.fromUriString(REDIRECT_URL)
                    .queryParam("token", token)
                    .queryParam("success", "true")
                    .build().toUriString();
                    
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", e);
            
            // 실패 페이지로 리다이렉트
            String errorUrl = UriComponentsBuilder.fromUriString(REDIRECT_URL)
                    .queryParam("error", "OAuth2 인증 처리 실패")
                    .queryParam("success", "false")
                    .build().toUriString();
                    
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * 요청에서 OAuth2 제공자 ID 추출
     */
    private String getRegistrationId(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        // /oauth2/callback/google 에서 google 추출
        String[] pathParts = requestUri.split("/");
        return pathParts[pathParts.length - 1];
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User getOrCreateUser(OAuth2UserInfo userInfo) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderId(
                userInfo.getProvider(), 
                userInfo.getProviderId()
        );

        if (existingUser.isPresent()) {
            // 기존 사용자인 경우 프로필 정보 업데이트
            User user = existingUser.get();
            user.updateOAuth2Profile(userInfo.getName(), userInfo.getImageUrl());
            user.recordLastLogin();
            return userRepository.save(user);
        } else {
            // 새 사용자 생성
            return createNewOAuth2User(userInfo);
        }
    }

    /**
     * 새로운 OAuth2 사용자 생성
     */
    private User createNewOAuth2User(OAuth2UserInfo userInfo) {
        // 닉네임 중복 처리
        String nickname = generateUniqueNickname(userInfo.getName(), userInfo.getProvider());
        
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .password(null) // OAuth2 사용자는 비밀번호가 없음
                .nickname(nickname)
                .role(UserRole.ROLE_USER)
                .status(UserStatus.ACTIVE) // OAuth2 사용자는 이메일 인증 없이 바로 활성화
                .profileImageUrl(userInfo.getImageUrl())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .build();

        User savedUser = userRepository.save(newUser);
        savedUser.recordLastLogin();
        
        log.info("새 OAuth2 사용자 생성 완료 - 제공자: {}, 이메일: {}", 
                userInfo.getProvider(), userInfo.getEmail());
                
        return userRepository.save(savedUser);
    }

    /**
     * 중복되지 않는 닉네임 생성
     */
    private String generateUniqueNickname(String baseName, String provider) {
        if (baseName == null || baseName.trim().isEmpty()) {
            baseName = provider + "사용자";
        }

        String nickname = baseName;
        int counter = 1;
        
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseName + counter;
            counter++;
        }
        
        return nickname;
    }
} 