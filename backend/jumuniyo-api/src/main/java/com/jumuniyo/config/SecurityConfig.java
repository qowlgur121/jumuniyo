package com.jumuniyo.config; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.security.JwtAuthenticationFilter;
import com.jumuniyo.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // CSRF, HttpBasic 등 비활성화 시 사용
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// 이 클래스가 Spring Security 설정 파일이고, 웹 보안 기능을 켠다는 것을 알려준다.
@Configuration
@EnableWebSecurity // Spring Security 활성화
@RequiredArgsConstructor // JwtAuthenticationFilter와 OAuth2AuthenticationSuccessHandler 의존성 주입을 위한 생성자 자동 생성
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // PasswordEncoder 빈 등록 (BCrypt 알고리즘 사용)
    // 이제 UserServiceImpl에서 @RequiredArgsConstructor를 통해 이 빈을 문제없이 주입받아 사용할 수 있게 된다.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 개발용 - 프로덕션에서는 특정 도메인으로 제한
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    //이 메소드는 HTTP 요청이 들어왔을 때 어떤 보안 검사를 할지 그 **'필터 체인'**을 설정하는 핵심 부분
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // CSRF(Cross-Site Request Forgery) 보호 비활성화 (Stateless API 서버에서는 보통 비활성화)
                .csrf(AbstractHttpConfigurer::disable) // 이전: .csrf().disable()

                // HTTP Basic 인증 비활성화 (토큰 기반 인증 사용 예정)
                .httpBasic(AbstractHttpConfigurer::disable) // 이전: .httpBasic().disable()

                // 폼 로그인 비활성화 (커스텀 로그인 페이지/API 사용 예정)
                .formLogin(AbstractHttpConfigurer::disable) // 이전: .formLogin().disable()

                // 세션 관리 정책: STATELESS (JWT 같은 토큰 기반 인증 시 세션 사용 안 함)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize") // OAuth2 인증 시작 URL
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*") // OAuth2 콜백 URL
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 인증 성공 시 커스텀 핸들러 사용
                )

                // 요청 경로별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근 가능한 경로들 (순서가 중요함)
                        .requestMatchers("/api/v1/auth/signup").permitAll()           // 회원가입 API
                        .requestMatchers("/api/v1/auth/login").permitAll()            // 로그인 API
                        .requestMatchers("/api/v1/auth/find-email").permitAll()       // 이메일 찾기 API
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()   // 비밀번호 재설정 요청 API
                        .requestMatchers("/api/v1/auth/reset-password/validate").permitAll() // 토큰 검증 API
                        .requestMatchers("/api/v1/auth/change-password").permitAll()  // 비밀번호 변경 API
                        .requestMatchers("/api/v1/owner/signup").permitAll()          // 사장님 회원가입 API
                        .requestMatchers("/api/v1/categories/**").permitAll()         // 카테고리 API (공개)
                        .requestMatchers("/api/test/**").permitAll()                  // 테스트 API (개발용)
                        .requestMatchers("/oauth2/**").permitAll()                    // OAuth2 관련 모든 경로
                        .requestMatchers("/actuator/**").permitAll()                  // Actuator 엔드포인트
                        .requestMatchers("/swagger-ui/**").permitAll()                // Swagger UI
                        .requestMatchers("/v3/api-docs/**").permitAll()               // OpenAPI 문서
                        .requestMatchers("/error").permitAll()                        // 에러 페이지
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                // 이렇게 하면 매 요청마다 JWT 토큰을 먼저 검증하고, 유효하면 인증 정보를 SecurityContext에 설정함
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}