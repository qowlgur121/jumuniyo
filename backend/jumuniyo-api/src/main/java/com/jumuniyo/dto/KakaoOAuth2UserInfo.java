package com.jumuniyo.dto;

import java.util.Map;

/**
 * Kakao OAuth2 사용자 정보 구현체
 * Kakao에서 제공하는 사용자 정보 형식에 맞춰 구현
 * Kakao는 kakao_account와 properties 객체 안에 사용자 정보가 들어있음
 */
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> properties;
    
    @SuppressWarnings("unchecked")
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.properties = (Map<String, Object>) attributes.get("properties");
    }
    
    @Override
    public String getProvider() {
        return "kakao";
    }
    
    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }
    
    @Override
    public String getEmail() {
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }
    
    @Override
    public String getName() {
        if (properties != null) {
            return (String) properties.get("nickname");
        }
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                return (String) profile.get("nickname");
            }
        }
        return null;
    }
    
    @Override
    public String getImageUrl() {
        if (properties != null) {
            return (String) properties.get("profile_image");
        }
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                return (String) profile.get("profile_image_url");
            }
        }
        return null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
} 