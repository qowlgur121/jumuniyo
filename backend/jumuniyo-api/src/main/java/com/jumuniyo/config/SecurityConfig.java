package com.jumuniyo.config; // 본인의 패키지 경로에 맞게 수정

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CSRF, HttpBasic 등 비활성화 시 사용
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// 이 클래스가 Spring Security 설정 파일이고, 웹 보안 기능을 켠다는 것을 알려준다.
@Configuration
@EnableWebSecurity // Spring Security 활성화
public class SecurityConfig {

    // PasswordEncoder 빈 등록 (BCrypt 알고리즘 사용)
    // 이제 UserServiceImpl에서 @RequiredArgsConstructor를 통해 이 빈을 문제없이 주입받아 사용할 수 있게 된다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    //이 메소드는 HTTP 요청이 들어왔을 때 어떤 보안 검사를 할지 그 **'필터 체인'**을 설정하는 핵심 부분
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // csrf, httpBasic, formLogin 비활성화: REST API 서버에서는 세션 기반 인증이나 기본적인 HTTP 인증 방식을 잘 사용하지 않기 때문에 관련 기능을 꺼둔다. 우리는 나중에 토큰 기반 인증(JWT)을 사용할 거다.
        http
                // CSRF(Cross-Site Request Forgery) 보호 비활성화 (Stateless API 서버에서는 보통 비활성화)
                .csrf(AbstractHttpConfigurer::disable) // 이전: .csrf().disable()

                // HTTP Basic 인증 비활성화 (토큰 기반 인증 사용 예정)
                .httpBasic(AbstractHttpConfigurer::disable) // 이전: .httpBasic().disable()

                // 폼 로그인 비활성화 (커스텀 로그인 페이지/API 사용 예정)
                .formLogin(AbstractHttpConfigurer::disable) // 이전: .formLogin().disable()

                // 세션 관리 정책: STATELESS (JWT 같은 토큰 기반 인증 시 세션 사용 안 함)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers( // 괄호 안에 나열된 URL 패턴들(회원가입, 로그인, API 문서 주소 등)은 로그인 여부와 관계없이 누구든 접근할 수 있도록 허용
                                "/api/v1/auth/signup", // 회원가입 API
                                "/api/v1/auth/login",  // 로그인 API (추후 구현)
                                "/swagger-ui/**",      // Swagger UI 접근 허용
                                "/v3/api-docs/**"      // OpenAPI 명세 접근 허용
                                // TODO: 필요한 다른 공개 API 경로 추가
                        ).permitAll() // 위 경로들은 인증 없이 누구나 접근 허용
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                );
        // TODO: JWT 인증 필터 추가 예정 (UsernamePasswordAuthenticationFilter 전에)

        return http.build();
    }
}