# 주문이요 앱 - 페이지 URL 가이드

## 🏠 메인 앱 (일반 고객용)

### 기본 탭 페이지
- `http://localhost:5173/` → 자동으로 `/tabs/tab1`로 리다이렉트
- `http://localhost:5173/tabs/tab1` → 홈 페이지 (메인 화면)
- `http://localhost:5173/tabs/tab2` → 탭 2 페이지
- `http://localhost:5173/tabs/tab3` → 탭 3 페이지  
- `http://localhost:5173/tabs/tab4` → 탭 4 페이지
- `http://localhost:5173/tabs/tab5` → 탭 5 페이지

## 🔐 일반 회원 인증

### 회원가입 & 로그인
- `http://localhost:5173/auth/signup` → 일반 회원가입
- `http://localhost:5173/auth/login` → 일반 로그인

### 계정 찾기 & 비밀번호 재설정
- `http://localhost:5173/auth/find-email` → 이메일 찾기
- `http://localhost:5173/auth/find-password` → 비밀번호 찾기
- `http://localhost:5173/auth/reset-password` → 비밀번호 재설정

## 👨‍💼 사장님 전용 페이지

### 사장님 인증
- `http://localhost:5173/owner/signup` → 사장님 회원가입
- `http://localhost:5173/owner/login` → 사장님 로그인

### 사장님 대시보드 & 관리
- `http://localhost:5173/owner/dashboard` → **사장님 메인 대시보드** ⭐
  - 가게 선택 및 관리
  - 영업 상태 토글 (영업중/휴업중)
  - 가게 삭제 기능
  - 통계 및 빠른 작업
  - 최근 주문 확인

- `http://localhost:5173/owner/orders` → 주문 관리
- `http://localhost:5173/owner/analytics` → 매출 분석
- `http://localhost:5173/owner/profile` → 사장님 프로필 설정

## 🏪 가게 관리

### 가게 등록 & 수정
- `http://localhost:5173/store/register` → 새 가게 등록
- `http://localhost:5173/store/edit/{가게ID}` → 가게 정보 수정
  - 예: `http://localhost:5173/store/edit/1`
  - 수정 완료 후 자동으로 사장님 대시보드로 이동

### 메뉴 관리
- `http://localhost:5173/store/{가게ID}/menu-management` → 메뉴 관리
  - 예: `http://localhost:5173/store/1/menu-management`
  - 사장님 대시보드에서 가게 선택 후 접근 가능

## 🔒 접근 권한 정보

### 인증 필요 페이지
다음 페이지들은 로그인이 필요합니다:
- 모든 `/owner/*` 페이지
- 모든 `/store/*` 페이지

### 사장님 전용 페이지
다음 페이지들은 사장님 계정으로만 접근 가능합니다:
- `/owner/dashboard`
- `/owner/orders`
- `/owner/analytics`
- `/owner/profile`
- `/store/{가게ID}/menu-management`

### 일반 사용자 접근 가능
- 모든 `/tabs/*` 페이지
- 모든 `/auth/*` 페이지
- `/store/register` (가게 등록)
- `/store/edit/{id}` (가게 수정)

## 🚀 주요 기능별 접근 경로

### 가게 운영 시작하기
1. `http://localhost:5173/owner/signup` → 사장님 회원가입
2. `http://localhost:5173/owner/login` → 사장님 로그인
3. `http://localhost:5173/owner/dashboard` → 대시보드에서 "가게 등록하기"
4. `http://localhost:5173/store/register` → 가게 정보 입력
5. 등록 완료 후 자동으로 대시보드로 이동

### 가게 관리하기
1. `http://localhost:5173/owner/dashboard` → 사장님 대시보드
2. 가게 선택 후 다음 작업 가능:
   - **영업 상태 토글**: 대시보드에서 직접 스위치 조작
   - **가게 정보 수정**: "가게 정보 수정" 버튼 클릭
   - **가게 삭제**: "가게 삭제" 버튼 클릭
   - **메뉴 관리**: "메뉴 관리" 카드 클릭

### 주문 관리하기
1. `http://localhost:5173/owner/dashboard` → 대시보드에서 주문 현황 확인
2. `http://localhost:5173/owner/orders` → 상세 주문 관리

## 📱 개발 서버 정보

- **프론트엔드**: `http://localhost:5173`
- **백엔드 API**: `http://localhost:8081/api/v1`
- **개발 환경**: Vue.js + Ionic + Spring Boot

## ⚠️ 주의사항

1. **권한 확인**: 사장님 전용 페이지는 `ROLE_OWNER` 권한이 필요합니다.
2. **승인 상태**: 사장님 계정이 `PENDING_APPROVAL` 상태면 접근이 제한됩니다.
3. **자동 리다이렉트**: 
   - 인증이 필요한 페이지에 비로그인 상태로 접근 시 로그인 페이지로 이동
   - 사장님 전용 페이지에 일반 사용자가 접근 시 사장님 로그인 페이지로 이동
4. **가게 선택**: 메뉴 관리 등 일부 기능은 대시보드에서 가게를 먼저 선택해야 합니다.

---

**최종 업데이트**: - 영업 상태 토글 및 가게 삭제 기능 추가 