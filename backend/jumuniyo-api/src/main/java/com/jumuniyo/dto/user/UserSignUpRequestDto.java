package com.jumuniyo.dto.user; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Controller에서 @RequestBody로 받을 때 Setter가 필요할 수 있음 (또는 생성자/빌더)

// Java 14 이상에서는 Record 타입을 사용하면 더 간결하게 DTO를 정의할 수 있다.
// public record UserSignUpRequestDto(String email, String password, String nickname) {}
// 여기서는 전통적인 클래스 방식으로

@Getter
@Setter
@NoArgsConstructor
public class UserSignUpRequestDto { // 사용자 회원가입 요청 시 클라이언트가 보낼 데이터를 담는 형식(DTO)임.

    @NotBlank(message = "이메일은 필수 입력 값입니다.") // 이메일 필드는 절대로 비어있으면 안 된다고 검사 조건을 붙이는 것임. 비어있으면 이 메시지를 보여줌.
    @Email(message = "이메일 형식이 올바르지 않습니다.") // 이메일 필드는 반드시 이메일 형식이어야 한다고 검사 조건을 붙이는 것임. 형식에 안 맞으면 이 메시지를 보여줌.
    @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.") // 이메일 필드는 길이가 100자를 넘으면 안 된다고 검사 조건을 붙이는 것임.
    private String email; // 사용자가 입력한 이메일 주소임.

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.") // 비밀번호 필드는 비어있으면 안 됨.
    // @Pattern 임: 비밀번호 필드는 이 복잡한 규칙(정규표현식)에 맞아야 한다고 검사 조건을 붙이는 것임. 이 규칙은 '최소 8자, 최대 20자, 영문/숫자/특수문자를 각각 하나 이상 포함해야 함'을 의미함.
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,20}$",
            message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 모두 포함해야 합니다.") // 규칙에 안 맞으면 이 메시지를 보여줌.
    private String password; // 사용자가 입력한 비밀번호임. (아직 암호화되기 전의 값)

    @NotBlank(message = "닉네임은 필수 입력 값입니다.") // 닉네임 필드는 비어있으면 안 됨.
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.") // 닉네임은 최소 2자, 최대 10자여야 함.
    @Pattern(regexp = "^[가-힣A-Za-z0-9]*$", message = "닉네임은 한글, 영문, 숫자만 사용 가능합니다.") // 닉네임은 한글, 영문, 숫자만 사용할 수 있음.
    private String nickname; // 사용자가 입력한 닉네임임.

    // 이 DTO에 담긴 데이터를 User Entity 객체로 변환해주는 기능임.
    // 데이터베이스에 저장하기 위해 DTO 형식을 Entity 형식으로 바꿔주는 것임.
    // 비밀번호는 여기서는 **아직 암호화하지 않음**. 암호화는 나중에 '서비스(Service)' 부분에서 할 것임.
    public User toEntity(String encodedPassword) { // 이 메소드를 호출할 때 **암호화된 비밀번호**를 파라미터로 넘겨줘야 함.
        return User.builder() // User Entity 클래스에 @Builder를 붙여둬서 이렇게 편하게 객체를 만들 수 있음. (아까 설명했음)
                .email(this.email) // 이 DTO의 이메일 값을 User Entity의 이메일로 설정함.
                .password(encodedPassword) // DTO의 password 필드(암호화 전)가 아니라, **암호화된 비밀번호**를 Entity의 password 필드에 설정함.
                .nickname(this.nickname) // 이 DTO의 닉네임 값을 User Entity의 닉네임으로 설정함.
                .role(UserRole.ROLE_USER) // 회원가입 시 기본 역할은 일반 사용자로 설정함. (서비스 정책에 따라 바뀔 수 있음. 예: 이메일 인증 전에는 GUEST)
                .status(UserStatus.PENDING_EMAIL_VERIFICATION) // 회원가입 직후 상태는 '이메일 인증 대기'로 설정함. (요구사항 FR-U01 반영)
                // .profileImageUrl(null) // 프로필 이미지는 나중에 등록하므로 일단 null 이나 기본 이미지 URL로 설정할 수 있음.
                .build(); // 설정이 끝났으니 User Entity 객체를 완성함.
    }

    // 이 DTO 객체를 빌더 패턴으로 쉽게 만들 수 있도록 하는 생성자임. (주로 테스트 코드 등에서 객체 만들 때 편리함)
    @Builder
    public UserSignUpRequestDto(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }
}