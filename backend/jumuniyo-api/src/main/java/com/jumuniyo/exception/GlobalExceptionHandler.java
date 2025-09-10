package com.jumuniyo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 데이터베이스 제약 조건 위반 예외 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("데이터베이스 제약 조건 위반: {}", ex.getMessage());
        
        String message = "처리 중 오류가 발생했습니다.";
        String detailedMessage = ex.getMessage();
        
        // 이메일 중복 오류 처리
        if (detailedMessage != null && detailedMessage.contains("users.UK6dotkott2kjsp8vw4d0m25fb7")) {
            message = "이미 가입된 이메일입니다. 다른 이메일을 사용해주세요.";
        }
        // 닉네임 중복 오류 처리
        else if (detailedMessage != null && detailedMessage.contains("nickname")) {
            message = "이미 사용 중인 닉네임입니다. 다른 닉네임을 사용해주세요.";
        }
        // 사업자등록번호 중복 오류 처리
        else if (detailedMessage != null && detailedMessage.contains("business_number")) {
            message = "이미 등록된 사업자등록번호입니다.";
        }
        // 전화번호 중복 오류 처리
        else if (detailedMessage != null && detailedMessage.contains("phone_number")) {
            message = "이미 등록된 전화번호입니다.";
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONSTRAINT_VIOLATION", message));
    }

    // SQL 제약 조건 위반 예외 처리
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL 제약 조건 위반: {}", ex.getMessage());
        
        String message = "이미 존재하는 정보입니다. 다른 값을 사용해주세요.";
        
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONSTRAINT_VIOLATION", message));
    }

    // 유효성 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        String message = "입력 정보를 확인해주세요.";
        log.error("유효성 검증 실패: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", message, errors));
    }

    // 일반적인 비즈니스 로직 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("비즈니스 로직 오류: {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    // 일반적인 런타임 예외 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("런타임 오류: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("예상치 못한 오류: {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("UNKNOWN_ERROR", "예상치 못한 오류가 발생했습니다. 관리자에게 문의해주세요."));
    }
} 