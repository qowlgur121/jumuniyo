package com.jumuniyo.domain.user; // 현재 파일이 속한 폴더(패키지) 이름임. 본인의 프로젝트 구조에 맞게 수정함.

import jakarta.persistence.*; // JPA 관련 특별한 의미를 가진 단어들(어노테이션)을 가져오는 부분임.
import lombok.AccessLevel; // Lombok 이라는 도구 관련 특별한 단어를 가져옴.
import lombok.Builder; // Lombok 빌더 기능 관련 단어를 가져옴.
import lombok.Getter; // Lombok Getter 기능 관련 단어를 가져옴.
import lombok.NoArgsConstructor; // Lombok 기본 생성자 기능 관련 단어를 가져옴.
import org.springframework.data.annotation.CreatedDate; // 스프링 데이터 JPA에서 생성 시간을 자동으로 채워주는 단어를 가져옴.
import org.springframework.data.annotation.LastModifiedDate; // 스프링 데이터 JPA에서 수정 시간을 자동으로 채워주는 단어를 가져옴.
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // 생성/수정 시간을 자동으로 관리해주는 기능 관련 단어를 가져옴.

import java.time.LocalDateTime; // 날짜와 시간을 다루는 자바 기본 기능을 가져옴.

@Entity // 이 클래스가 데이터베이스 테이블과 연결되는 '설계도(Entity)' 라는 것을 JPA에게 알려주는 것임.
@Table(name = "users") // 이 설계도대로 만들 데이터베이스 테이블 이름을 "users" 라고 정하는 것임.
@Getter // Lombok: 이 클래스 안에 있는 모든 정보(필드)들을 가져가는(Get) 메소드들을 자동으로 만들어줌.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 아무 파라미터도 없는 '기본 생성자'를 자동으로 만들어줌. JPA가 꼭 필요로 하는데, 외부에서 함부로 못 만들게 접근 권한은 Protected 로 하는 것이 좋음.
@EntityListeners(AuditingEntityListener.class) // 이 Entity의 변화(만들어지거나 수정될 때)를 감지해서 특정 작업(예: 시간 기록)을 자동으로 해주는 기능을 연결하는 것임.
public class User {

    @Id // 이 필드(id)가 테이블의 '주민등록번호' 같은 '기본 키(Primary Key)' 임을 나타내는 것임. 이걸로 각 행을 구분함.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 이 기본 키(id) 값은 우리가 직접 넣는게 아니라 데이터베이스가 알아서 순서대로(자동 증가) 만들어주세요~ 라고 맡기는 것임. (MySQL의 AUTO_INCREMENT 기능과 비슷함)
    @Column(name = "user_id") // 데이터베이스 테이블에서는 이 필드 이름을 "user_id" 라는 컬럼 이름으로 쓰겠다고 정하는 것임. (안 적으면 보통 필드 이름 id 그대로 사용함)
    private Long id; // 사용자의 고유 번호 (숫자가 커질 수 있으니 Long 타입을 사용함)

    @Column(nullable = false, unique = true, length = 100) // 데이터베이스 컬럼 설정임. '반드시 값이 있어야 함(nullable=false)', '다른 값과 중복되면 안 됨(unique=true)', '최대 100자까지 허용함(length=100)' 이라는 뜻임.
    private String email; // 사용자 이메일

    @Column(nullable = false) // '반드시 값이 있어야 함(nullable=false)' 설정임.
    private String password; // 암호화된 비밀번호를 저장할 것임. (실제 비밀번호가 아니라 변환된 값을 넣을 것임)

    @Column(nullable = false, unique = true, length = 50) // '반드시 값이 있어야 함', '중복되면 안 됨', '최대 50자' 설정임.
    private String nickname; // 사용자 닉네임

    @Enumerated(EnumType.STRING) // 아래 role 이라는 필드는 UserRole 이라는 정해진 목록(Enum) 타입인데, 데이터베이스에는 그 목록의 '이름'(예: "ROLE_USER")을 문자열로 저장하라는 뜻임. (EnumType.ORDINAL 은 순서 숫자로 저장해서 나중에 목록 순서 바뀌면 문제가 될 수 있음)
    @Column(nullable = false, length = 20) // '반드시 값이 있어야 함', '최대 20자' 설정임.
    private UserRole role; // 사용자의 역할 (예: 일반 사용자, 사장님, 관리자 등)

    @Enumerated(EnumType.STRING) // UserStatus 라는 정해진 목록(Enum) 타입인데, 데이터베이스에는 그 목록의 '이름'(예: "ACTIVE")을 문자열로 저장하라는 뜻임.
    @Column(nullable = false, length = 20) // '반드시 값이 있어야 함', '최대 20자' 설정임.
    private UserStatus status; // 사용자의 계정 상태 (예: 활성, 비활성, 이메일 인증 대기 등)

    private String profileImageUrl; // 프로필 이미지 주소 (이건 필수가 아님)

    @CreatedDate // @EntityListeners(AuditingEntityListener.class) 덕분에, 이 Entity가 데이터베이스에 '처음 저장될 때' 현재 시간이 자동으로 여기에 기록됨.
    @Column(nullable = false, updatable = false) // '반드시 값이 있어야 함', '한번 저장되면 나중에 절대로 수정될 수 없음(updatable=false)' 설정임.
    private LocalDateTime createdAt; // 계정이 만들어진 시간

    @LastModifiedDate // @EntityListeners(AuditingEntityListener.class) 덕분에, 이 Entity가 데이터베이스에서 '수정될 때마다' 현재 시간이 여기에 자동으로 기록됨.
    @Column(nullable = false) // '반드시 값이 있어야 함' 설정임.
    private LocalDateTime updatedAt; // 계정 정보가 마지막으로 수정된 시간

    private LocalDateTime lastLoginAt; // 마지막으로 로그인한 시간 (이건 필수가 아님)

    // 생성자 부분임 (객체를 만들 때 초기 값들을 넣어주는 역할임)
    // @Builder 라는 Lombok 기능을 사용하면, 좀 더 보기 좋고 안전하게 객체를 만들 수 있음.
    @Builder // Lombok: 이 생성자를 사용해서 User 객체를 만들 때 'User.builder().email("...").password("...").build()' 와 같은 형태로 만들 수 있게 해줌.
    public User(String email, String password, String nickname, UserRole role, UserStatus status, String profileImageUrl) {
        this.email = email;
        this.password = password; // 여기서 받는 비밀번호는 이미 암호화된 상태의 값이어야 함.
        this.nickname = nickname;
        this.role = role;
        this.status = status;
        this.profileImageUrl = profileImageUrl;
    }

    // --- 편의 메소드 (이 엔티티 객체 자체와 관련된 간단한 기능들을 여기에 추가할 수 있음) ---
    public void updateNickname(String nickname) { // 닉네임을 변경하는 기능임.
        this.nickname = nickname;
    }

    public void updatePassword(String newPassword) { // 비밀번호를 변경하는 기능임 (여기서 받는 값도 암호화된 값이어야 함).
        this.password = newPassword;
    }

    public void updateProfileImageUrl(String profileImageUrl) { // 프로필 이미지를 변경하는 기능임.
        this.profileImageUrl = profileImageUrl;
    }

    public void updateUserStatus(UserStatus status) { // 사용자 상태를 변경하는 기능임.
        this.status = status;
    }

    public void recordLastLogin() { // 마지막 로그인 시간을 현재 시간으로 기록하는 기능임.
        this.lastLoginAt = LocalDateTime.now();
    }

    // 이메일 인증이 완료되었을 때 상태를 변경하는 예시 기능임.
    public void completeEmailVerification() {
        if (this.status == UserStatus.PENDING_EMAIL_VERIFICATION) { // 현재 상태가 '이메일 인증 대기' 라면
            this.status = UserStatus.ACTIVE; // 상태를 '활성'으로 바꾸고
            this.role = UserRole.ROLE_USER; // 역할을 '일반 사용자'로 바꾸는 예시 로직임.
        }
    }
}