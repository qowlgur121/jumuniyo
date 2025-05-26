package com.jumuniyo.service.user; // 본인의 패키지 경로에 맞게 수정

import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.user.UserRole;
import com.jumuniyo.domain.user.UserStatus;
import com.jumuniyo.dto.user.UserSignUpRequestDto;
import com.jumuniyo.dto.user.UserLoginRequestDto;
import com.jumuniyo.dto.user.UserLoginResponseDto;
import com.jumuniyo.dto.auth.OwnerSignUpRequestDto;
import com.jumuniyo.dto.auth.OwnerSignUpResponseDto;
import com.jumuniyo.repository.user.UserRepository;
import com.jumuniyo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service // 이 클래스가 비즈니스 로직을 처리하는 서비스 레이어 컴포넌트임을 나타냄
@RequiredArgsConstructor // final 필드 또는 @NonNull 필드에 대한 생성자를 자동으로 생성 (의존성 주입)
@Transactional(readOnly = true) // 클래스 레벨에서는 기본적으로 읽기 전용 트랜잭션 적용 (성능 최적화)
// 데이터 변경이 있는 메소드에는 @Transactional을 개별적으로 붙여야 함
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository; // 생성자 주입
    private final PasswordEncoder passwordEncoder; // 생성자 주입 (Spring Security 설정에서 빈으로 등록 예정)
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성을 위한 의존성 주입

    @Override
    @Transactional // 데이터 변경이 있으므로 트랜잭션 적용
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

    @Override
    @Transactional // 데이터 변경이 있으므로 트랜잭션 적용
    public OwnerSignUpResponseDto ownerSignUp(OwnerSignUpRequestDto requestDto) {
        // 1. 이미 같은 이메일로 사장님 계정이 있는지 확인
        Optional<User> existingOwner = userRepository.findByEmailAndRole(requestDto.getEmail(), UserRole.ROLE_OWNER);
        if (existingOwner.isPresent()) {
            throw new IllegalArgumentException("이미 사장님 계정으로 가입된 이메일입니다: " + requestDto.getEmail());
        }

        // 2. 닉네임 중복 검사 (전체 사용자 대상)
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다: " + requestDto.getNickname());
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 4. User 엔티티 생성 (기본 사용자 정보)
        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .role(UserRole.ROLE_OWNER) // 사장님 역할 설정
                .status(UserStatus.ACTIVE) // 활성 상태
                .profileImageUrl(null)
                .provider(null) // 일반 가입 (OAuth2 아님)
                .providerId(null)
                .build();

        // 5. User 저장
        User savedUser = userRepository.save(user);

        // 6. 응답 DTO 생성 및 반환
        return OwnerSignUpResponseDto.of(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getPhoneNumber(),
                savedUser.getRole(),
                savedUser.getStatus(), // 사용자 상태
                savedUser.getCreatedAt()
        );
    }

    @Override
    @Transactional // 로그인 시간 업데이트를 위한 트랜잭션
    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다: " + requestDto.getEmail()));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 사용자 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성화된 계정입니다. 관리자에게 문의해주세요.");
        }

        // 4. 마지막 로그인 시간 업데이트
        user.recordLastLogin();

        // 5. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());

        // 6. 응답 DTO 생성 및 반환
        return UserLoginResponseDto.of(token, user);
    }

    // TODO: 추후 로그인, 회원정보 조회/수정 등의 메소드 구현
}