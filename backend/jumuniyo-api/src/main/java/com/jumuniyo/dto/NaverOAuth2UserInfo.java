package com.jumuniyo.dto;

import java.util.Map;

/**
 * Naver OAuth2 사용자 정보 구현체
 * Naver에서 제공하는 사용자 정보 형식에 맞춰 구현
 * Naver는 response 객체 안에 실제 사용자 정보가 들어있음
 */
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    private final Map<String, Object> response;
    
    @SuppressWarnings("unchecked")
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }
    
    @Override
    public String getProvider() {
        return "naver";
    }
    
    @Override
    public String getProviderId() {
        return response != null ? (String) response.get("id") : null;
    }
    
    @Override
    public String getEmail() {
        return response != null ? (String) response.get("email") : null;
    }
    
    @Override
    public String getName() {
        return response != null ? (String) response.get("name") : null;
    }
    
    @Override
    public String getImageUrl() {
        return response != null ? (String) response.get("profile_image") : null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
} 