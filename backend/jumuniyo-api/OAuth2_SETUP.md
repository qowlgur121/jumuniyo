# OAuth2 소셜 로그인 설정 가이드

## 개요
주문이요 앱에서 Google, Naver, Kakao OAuth2 소셜 로그인을 설정하는 방법을 설명합니다.

## 현재 상태
- ✅ 백엔드 OAuth2 연동 코드 구현 완료
- ✅ 프론트엔드 소셜 로그인 UI 구현 완료
- ⏳ 각 소셜 서비스 클라이언트 ID/Secret 설정 필요

## 설정이 필요한 소셜 서비스

### 1. Google OAuth2 설정

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. "API 및 서비스" > "OAuth 동의 화면" 설정
4. "API 및 서비스" > "사용자 인증 정보" > "사용자 인증 정보 만들기" > "OAuth 2.0 클라이언트 ID"
5. 애플리케이션 유형: 웹 애플리케이션
6. 승인된 리디렉션 URI: `http://localhost:8081/oauth2/callback/google`
7. 클라이언트 ID와 클라이언트 보안 비밀 복사

### 2. Naver OAuth2 설정

1. [네이버 개발자 센터](https://developers.naver.com/) 접속
2. "Application" > "애플리케이션 등록" 클릭
3. 애플리케이션 정보 입력:
   - 애플리케이션 이름: 주문이요
   - 사용 API: 네이버 로그인
   - 제공 정보: 이메일, 닉네임, 프로필 사진
4. 서비스 URL: `http://localhost:5173`
5. Callback URL: `http://localhost:8081/oauth2/callback/naver`
6. 클라이언트 ID와 클라이언트 시크릿 복사

### 3. Kakao OAuth2 설정

1. [Kakao Developers](https://developers.kakao.com/) 접속
2. "내 애플리케이션" > "애플리케이션 추가하기"
3. 앱 설정 > 플랫폼 설정에서 Web 플랫폼 등록
   - 사이트 도메인: `http://localhost:5173`
4. 제품 설정 > 카카오 로그인 설정
   - 카카오 로그인 활성화
   - Redirect URI: `http://localhost:8081/oauth2/callback/kakao`
5. 제품 설정 > 카카오 로그인 > 동의항목에서 필요한 정보 설정
   - 닉네임, 카카오계정(이메일), 프로필 사진
6. 앱 키 > REST API 키 복사 (클라이언트 ID로 사용)
7. 보안 > Client Secret 생성 및 복사

## 환경변수 설정

### 개발 환경 (.env 파일 또는 IDE 설정)
```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Naver OAuth2
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-rest-api-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

### 운영 환경
운영 환경에서는 실제 도메인으로 Redirect URI를 변경해야 합니다:
- `https://yourdomain.com/oauth2/callback/google`
- `https://yourdomain.com/oauth2/callback/naver`
- `https://yourdomain.com/oauth2/callback/kakao`

## 테스트 방법

1. 환경변수 설정 완료 후 백엔드 서버 재시작
2. 프론트엔드에서 소셜 로그인 버튼 클릭
3. 각 소셜 서비스 인증 페이지로 리다이렉트 확인
4. 인증 완료 후 JWT 토큰 발급 확인
5. 사용자 정보가 데이터베이스에 저장되었는지 확인

## 주의사항

- 개발 환경에서는 `http://localhost` 사용 가능
- 운영 환경에서는 반드시 HTTPS 사용 필요
- 각 소셜 서비스마다 제공하는 사용자 정보 형식이 다름
- 카카오의 경우 사업자 등록이 완료된 앱만 이메일 정보 제공 (개발 단계에서는 제한적)

## 트러블슈팅

### 일반적인 문제들
1. **Redirect URI 불일치**: 각 소셜 서비스에서 설정한 URI와 코드의 URI가 정확히 일치하는지 확인
2. **클라이언트 ID/Secret 오타**: 환경변수 값 재확인
3. **권한 설정**: 각 소셜 서비스에서 필요한 권한(scope)이 설정되었는지 확인
4. **CORS 에러**: 프론트엔드와 백엔드 간 CORS 설정 확인

### 로그 확인
```yaml
logging:
  level:
    org.springframework.security.oauth2: DEBUG
    org.springframework.web.client: DEBUG
```

## 현재 구현된 백엔드 엔드포인트

- `GET /oauth2/authorization/{provider}` - OAuth2 인증 시작
- `GET /oauth2/callback/{provider}` - OAuth2 콜백 처리
- `GET /api/v1/oauth2/authorize/{provider}` - 커스텀 OAuth2 시작점
- `GET /api/v1/oauth2/providers` - 지원하는 소셜 로그인 서비스 목록

## 추가 구현 예정 기능

- [ ] Apple OAuth2 지원 (iOS 앱 출시 시)
- [ ] 소셜 로그인 계정 연결/해제 기능
- [ ] 소셜 로그인 실패 시 에러 페이지
- [ ] 이메일 인증이 필요한 소셜 로그인 처리 