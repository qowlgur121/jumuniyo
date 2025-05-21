package com.jumuniyo.service.user; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.dto.user.UserSignUpRequestDto;
// import com.jumuniyo.domain.user.User; // 필요하다면 User 엔티티 직접 반환

public interface UserService {

    /**
     * 사용자 회원가입 처리
     * @param requestDto 회원가입 요청 정보 DTO
     * @return 생성된 사용자 정보 (또는 void, 또는 성공 메시지 등 상황에 따라)
     * @throws IllegalArgumentException 이메일 또는 닉네임 중복 시
     */
    void signUp(UserSignUpRequestDto requestDto); // 여기서는 void로 처리하고 Controller에서 성공 응답

    // TODO: 추후 로그인, 회원정보 조회/수정 등의 메소드 추가 예정
}