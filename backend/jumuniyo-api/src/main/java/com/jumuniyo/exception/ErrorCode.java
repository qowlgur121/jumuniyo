package com.jumuniyo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "잘못된 타입 값입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근이 거부되었습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "잘못된 비밀번호입니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U004", "이미 존재하는 사용자입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "음식점을 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "S002", "음식점에 대한 접근 권한이 없습니다."),
    DUPLICATE_STORE_NAME(HttpStatus.CONFLICT, "S003", "이미 사용 중인 음식점 이름입니다."),

    // Menu Category
    MENU_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "MC001", "메뉴 카테고리를 찾을 수 없습니다."),
    DUPLICATE_MENU_CATEGORY_NAME(HttpStatus.CONFLICT, "MC002", "이미 사용 중인 카테고리 이름입니다."),
    MENU_CATEGORY_HAS_MENUS(HttpStatus.BAD_REQUEST, "MC003", "메뉴가 있는 카테고리는 삭제할 수 없습니다."),

    // Menu
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "메뉴를 찾을 수 없습니다."),
    DUPLICATE_MENU_NAME(HttpStatus.CONFLICT, "M002", "이미 사용 중인 메뉴 이름입니다."),
    MENU_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "M003", "판매 중단된 메뉴입니다."),

    // Menu Option
    MENU_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MO001", "메뉴 옵션을 찾을 수 없습니다."),
    MENU_OPTION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "MOG001", "메뉴 옵션 그룹을 찾을 수 없습니다."),

    // Authentication
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A001", "인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A004", "리프레시 토큰을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
} 