package com.jumuniyo.controller.dev;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test") // 모든 요청은 /api/test 로 시작
@Tag(name = "개발/테스트 API", description = "개발 및 테스트 목적으로 사용되는 API")
public class TestController {

    @GetMapping("/hello") // GET /api/test/hello
    @Operation(summary = "테스트용 Hello API", description = "간단한 문자열 응답을 반환하여 서버 동작 및 CORS 설정을 테스트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적인 응답", 
                         content = @Content(mediaType = "application/json", 
                                          schema = @Schema(type = "object", example = "{\"message\": \"안녕하세요! 백엔드에서 보내는 응답입니다. CORS 테스트 성공!\"}")))
    })
    public Map<String, String> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "안녕하세요! 백엔드에서 보내는 응답입니다. CORS 테스트 성공!");
        return response;
    }
}