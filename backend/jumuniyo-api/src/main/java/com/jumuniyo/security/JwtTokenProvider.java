package com.jumuniyo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long tokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret-key:defaultSecretKeyForJumuniyoApplicationThatIsLongEnoughForHS256Algorithm}") String secretKey,
            @Value("${jwt.token-validity-in-seconds:86400}") long tokenValidityInSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    // JWT 토큰 생성
    public String createToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // JWT 토큰에서 이메일 추출
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    // JWT 토큰에서 역할 추출
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // JWT 토큰에서 Claims 추출
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
} 