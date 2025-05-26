package com.jumuniyo.config; // 본인의 패키지 경로에 맞게 수정

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// implements WebMvcConfigurer: 나는 '웹 설정을 바꾸는 능력'을 사용할 거야! 라고 선언하는 것임.
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    // WebMvcConfigurer 인터페이스에 정의된 '웹 설정 변경 능력' 중,
    // 'CORS(다른 출처 요청 허용) 규칙을 추가하는' 능력을 사용할 거야! 라고 선언하고 그 내용을 작성하는 부분임.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 백엔드로 들어오는 모든 '주소(URL 경로)'에 대해
                .allowedOrigins(
                        "http://localhost:8100", // Ionic Vue 개발 서버 (ionic serve)
                        "http://localhost:5173", // 일반 Vite 개발 서버 (npm run dev) - 혹시 사용할 경우 대비
                        "capacitor://localhost",  // Capacitor로 만든 Android나 iOS 앱 안의 웹 화면이 자신을 나타낼 때 사용하는 '가상 주소(Origin)' 중 하나임.
                        "ionic://localhost"       // 특히 Ionic Framework와 함께 Capacitor로 만든 iOS 앱 안의 웹 화면이 사용하는 '가상 주소(Origin)' 중 하나임.
                        // TODO: 추후 실제 배포될 프론트엔드 도메인 추가 필요
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메소드
                .allowedHeaders("*") // 모든 요청 헤더 허용 (커스텀 헤더 포함)
                .allowCredentials(true) // 쿠키/인증 정보를 포함한 요청 허용
                .maxAge(3600); // Preflight 요청 결과를 캐시할 시간 (초 단위)
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일들을 정적 리소스로 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}