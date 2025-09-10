package com.jumuniyo.integration;

import com.jumuniyo.JumuniyoApiApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = JumuniyoApiApplication.class)
@ActiveProfiles("test")
@DisplayName("최소 통합 테스트")
public class MinimalIntegrationTest {

    @Test
    @DisplayName("애플리케이션 컨텍스트 로드 테스트")
    void contextLoads() {
        // 이 테스트는 컨텍스트가 성공적으로 로드되면 통과합니다.
    }
} 