package com.jumuniyo.service.auth;

import com.jumuniyo.domain.user.PasswordResetToken;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.auth.*;
import com.jumuniyo.repository.user.PasswordResetTokenRepository;
import com.jumuniyo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * 계정 복구 서비스 (이메일 찾기, 비밀번호 재설정)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountRecoveryService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    // private final EmailService emailService; // 이메일 서비스는 추후 구현

    /**
     * 이메일 찾기
     */
    public FindEmailResponse findEmail(FindEmailRequest request) {
        log.info("이메일 찾기 요청 - 닉네임: {}, 전화번호: {}", request.getNickname(), request.getPhoneNumber());

        Optional<User> userOptional = userRepository.findByNicknameAndPhoneNumber(
                request.getNickname(), 
                request.getPhoneNumber()
        );

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String maskedEmail = user.getMaskedEmail();
            log.info("이메일 찾기 성공 - 마스킹된 이메일: {}", maskedEmail);
            return FindEmailResponse.success(maskedEmail);
        } else {
            log.warn("이메일 찾기 실패 - 일치하는 사용자 없음");
            return FindEmailResponse.notFound();
        }
    }

    /**
     * 비밀번호 재설정 토큰 생성 및 이메일 전송
     */
    @Transactional
    public void requestPasswordReset(ResetPasswordRequest request) {
        log.info("비밀번호 재설정 요청 - 이메일: {}", request.getEmail());

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            log.warn("비밀번호 재설정 요청 실패 - 존재하지 않는 이메일: {}", request.getEmail());
            // 보안상 이유로 사용자에게는 성공 메시지를 보여줌
            return;
        }

        User user = userOptional.get();

        // OAuth2 사용자는 비밀번호 재설정 불가
        if (user.isOAuth2User()) {
            log.warn("OAuth2 사용자 비밀번호 재설정 시도 - 이메일: {}", request.getEmail());
            return;
        }

        // 기존 토큰들을 모두 사용됨으로 표시
        passwordResetTokenRepository.markAllUserTokensAsUsed(user);

        // 새 토큰 생성
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // 1시간 후 만료

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // TODO: 이메일 전송 서비스 구현
        // emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("비밀번호 재설정 토큰 생성 완료 - 사용자 ID: {}, 토큰: {}", user.getId(), token);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        log.info("비밀번호 변경 요청 - 토큰: {}", request.getToken());

        // 비밀번호 확인 검증
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            log.warn("비밀번호 변경 실패 - 비밀번호 불일치");
            return false;
        }

        // 토큰 조회 및 검증
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());
        if (tokenOptional.isEmpty()) {
            log.warn("비밀번호 변경 실패 - 존재하지 않는 토큰: {}", request.getToken());
            return false;
        }

        PasswordResetToken resetToken = tokenOptional.get();
        if (!resetToken.isValid()) {
            log.warn("비밀번호 변경 실패 - 유효하지 않은 토큰: {}", request.getToken());
            return false;
        }

        // 비밀번호 변경
        User user = resetToken.getUser();
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedPassword);
        userRepository.save(user);

        // 토큰을 사용됨으로 표시
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);

        log.info("비밀번호 변경 완료 - 사용자 ID: {}", user.getId());
        return true;
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        return tokenOptional.isPresent() && tokenOptional.get().isValid();
    }

    /**
     * 만료된 토큰들 정리 (스케줄러에서 호출)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("만료된 비밀번호 재설정 토큰 정리 완료");
    }
} 