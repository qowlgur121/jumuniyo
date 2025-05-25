package com.jumuniyo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors; // 유효성 검증 오류 등 상세 정보

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String code, String message, Map<String, String> errors) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
} 