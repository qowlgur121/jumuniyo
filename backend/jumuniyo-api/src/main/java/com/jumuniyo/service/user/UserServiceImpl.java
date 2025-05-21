package com.jumuniyo.service.user; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.user.UserSignUpRequestDto;
import com.jumuniyo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 이 클래스가 비즈니스 로직을 처리하는 서비스 레이어 컴포넌트임을 나타냄
@RequiredArgsConstructor // final 필드 또는 @NonNull 필드에 대한 생성자를 자동으로 생성 (의존성 주입)
@Transactional(readOnly = true) // 클래스 레벨에서는 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화)
// 데이터 변경이 있는 메소드에는 @Transactional을 개별적으로 붙여야 함
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // 생성자 주입
    private final PasswordEncoder passwordEncoder; // 생성자 주입 (Spring Security 설정에서 빈으로 등록 예정)

    @Override
    @Transactional // 데이터 변경이 있으므로 트랜잭션 적용 (readOnly = false가 기본값)
    public void signUp(UserSignUpRequestDto requestDto) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + requestDto.getEmail());
        }

        // 2. 닉네임 중복 검사
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + requestDto.getNickname());
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 4. DTO를 Entity로 변환하여 저장
        User user = requestDto.toEntity(encodedPassword); // DTO의 toEntity 메소드 활용
        userRepository.save(user);

        // TODO: (FR-U01) 이메일 인증 메일 발송 로직 추가 예정 (Spring Mail 사용)
        // 이 경우, User 엔티티의 status는 PENDING_EMAIL_VERIFICATION으로 유지됨
        // 현재 UserSignUpRequestDto.toEntity()에서 초기 상태를 PENDING_EMAIL_VERIFICATION으로 설정함

        // 회원가입 성공 후 추가 작업이 있다면 여기에 구현 (예: 알림, 로그 기록 등)
    }

    // TODO: 추후 로그인, 회원정보 조회/수정 등의 메소드 구현
}