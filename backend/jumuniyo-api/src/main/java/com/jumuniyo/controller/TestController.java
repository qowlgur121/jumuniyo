package com.jumuniyo.controller; // 실제 패키지 경로에 맞게 수정

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test") // 모든 요청은 /api/test 로 시작
public class TestController {

    @GetMapping("/hello") // GET /api/test/hello
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "안녕하세요! 백엔드에서 보내는 응답입니다. CORS 테스트 성공!");
        return response;
    }
}