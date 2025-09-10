package com.jumuniyo.dto;

import java.util.Map;

/**
 * OAuth2 소셜 로그인 사용자 정보를 추상화하는 인터페이스
 * 각 소셜 서비스(Google, Naver, Kakao)의 사용자 정보 형식이 다르므로
 * 공통 인터페이스로 통일된 접근을 제공합니다.
 */
public interface OAuth2UserInfo {
    
    /**
     * OAuth2 제공자 이름 반환 (google, naver, kakao)
     */
    String getProvider();
    
    /**
     * 제공자에서 제공하는 고유 사용자 ID
     */
    String getProviderId();
    
    /**
     * 사용자 이메일
     */
    String getEmail();
    
    /**
     * 사용자 이름
     */
    String getName();
    
    /**
     * 프로필 이미지 URL
     */
    String getImageUrl();
    
    /**
     * 원본 사용자 정보 attributes
     */
    Map<String, Object> getAttributes();
} 