package com.jumuniyo.dto;

import java.util.Map;

/**
 * OAuth2 제공자별로 적절한 사용자 정보 구현체를 생성하는 팩토리 클래스
 */
public class OAuth2UserInfoFactory {
    
    /**
     * 제공자 이름에 따라 적절한 OAuth2UserInfo 구현체를 반환
     *
     * @param registrationId OAuth2 제공자 등록 ID (google, naver, kakao)
     * @param attributes     OAuth2 사용자 정보 attributes
     * @return OAuth2UserInfo 구현체
     * @throws IllegalArgumentException 지원하지 않는 제공자인 경우
     */
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
        }
    }
} 