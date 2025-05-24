package com.jumuniyo.dto;

import java.util.Map;

/**
 * Google OAuth2 사용자 정보 구현체
 * Google에서 제공하는 사용자 정보 형식에 맞춰 구현
 */
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getProvider() {
        return "google";
    }
    
    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }
    
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    
    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
    
    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
} 