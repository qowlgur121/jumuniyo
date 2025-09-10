package com.jumuniyo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 토큰을 검증하고 Spring Security 컨텍스트에 인증 정보를 설정하는 필터
 * 모든 HTTP 요청에 대해 한 번씩 실행됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = resolveToken(request);
            
            // 2. 토큰이 있고 유효한지 검증
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 사용자 정보 추출
                String email = jwtTokenProvider.getEmailFromToken(token);
                String role = jwtTokenProvider.getRoleFromToken(token);
                
                // 4. Spring Security Authentication 객체 생성
                Authentication authentication = createAuthentication(email, role);
                
                // 5. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("JWT 인증 성공: email={}, role={}", email, role);
            } else {
                log.debug("유효하지 않은 JWT 토큰: {}", token);
            }
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생", e);
            // 인증 실패 시 SecurityContext 클리어
            SecurityContextHolder.clearContext();
        }
        
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출합니다.
     * Bearer {token} 형식에서 토큰 부분만 반환합니다.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }

    /**
     * 사용자 이메일과 역할을 기반으로 Spring Security Authentication 객체를 생성합니다.
     */
    private Authentication createAuthentication(String email, String role) {
        // SimpleGrantedAuthority를 사용하여 권한 생성
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        
        // UsernamePasswordAuthenticationToken 생성 (인증된 상태로)
        return new UsernamePasswordAuthenticationToken(email, null, authorities);
    }
} 