package com.jumuniyo.domain.user; // 현재 파일이 속한 폴더(패키지) 이름임.

import lombok.Getter; // Lombok Getter 기능 관련 단어를 가져옴.
import lombok.RequiredArgsConstructor; // Lombok: 특정 필드들만 사용하는 생성자를 자동으로 만들어주는 단어를 가져옴.

@Getter // Lombok: 이 Enum 의 'description' 필드에 대한 Getter 메소드를 자동으로 만들어줌.
@RequiredArgsConstructor // Lombok: final 이나 @NonNull 이 붙은 필드들을 파라미터로 받는 생성자를 자동으로 만들어줌. 여기서는 description 필드에 대한 생성자를 만들어줌.
public enum UserRole { // UserRole 이라는 정해진 목록(Enum)을 정의하는 것임.
    ROLE_USER("일반 사용자"), // 'ROLE_USER' 라는 항목을 정의하고, 거기에 '일반 사용자' 라는 설명을 붙여놓는 것임.
    ROLE_OWNER("사장님"),
    ROLE_ADMIN("관리자"),
    ROLE_GUEST("게스트"); // 비회원 또는 아직 역할이 정해지지 않은 상태를 나타내는 것임.

    private final String description; // 각 역할에 대한 설명을 저장할 필드임. final 이 붙어서 RequiredArgsConstructor 로 생성자가 만들어짐.
}