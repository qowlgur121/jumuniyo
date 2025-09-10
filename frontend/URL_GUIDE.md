# 주문이요 앱 - 페이지 URL 가이드

## 🏠 메인 앱 (고객용)
- **홈**: `http://localhost:5173/` → `/tabs/tab1`
- **카테고리**: `http://localhost:5173/tabs/tab2`
- **검색**: `http://localhost:5173/tabs/tab3`
- **주문내역**: `http://localhost:5173/tabs/tab4`
- **마이페이지**: `http://localhost:5173/tabs/tab5`

## 🔐 일반 회원 인증
- **회원가입**: `http://localhost:5173/auth/signup`
- **로그인**: `http://localhost:5173/auth/login`
- **이메일 찾기**: `http://localhost:5173/auth/find-email`
- **비밀번호 찾기**: `http://localhost:5173/auth/find-password`
- **비밀번호 재설정**: `http://localhost:5173/auth/reset-password`

## 👨‍💼 사장님 인증
- **사장님 회원가입**: `http://localhost:5173/owner/signup`
- **사장님 로그인**: `http://localhost:5173/owner/login`

## 📊 사장님 대시보드 (인증 필요)
- **메인 대시보드**: `http://localhost:5173/owner/dashboard`
  - 가게 선택 및 관리
  - 영업 상태 토글 (영업중/휴업중)
  - 가게 삭제 기능
  - 통계 및 빠른 작업
- **주문 관리**: `http://localhost:5173/owner/orders`
- **매출 분석**: `http://localhost:5173/owner/analytics`
- **프로필 설정**: `http://localhost:5173/owner/profile`

## 🏪 가게 관리 (사장님 전용, 인증 필요)
- **가게 등록**: `http://localhost:5173/store/register`
  - 완료 후 자동으로 대시보드로 이동
- **가게 정보 수정**: `http://localhost:5173/store/edit/{가게ID}`
  - 완료 후 자동으로 대시보드로 이동
  - 예시: `http://localhost:5173/store/edit/1`

## 🔒 접근 권한 정보

### 인증 불필요 (누구나 접근 가능)
- 메인 앱 (tabs)
- 일반 회원 인증 페이지
- 사장님 인증 페이지

### 일반 인증 필요
- 가게 등록/수정 페이지

### 사장님 인증 필요 (ROLE_OWNER + 승인 완료)
- 사장님 대시보드
- 주문 관리
- 매출 분석
- 프로필 설정

## 🚀 개발 서버 실행
```bash
cd frontend
npm run dev
```

## 📝 주요 기능

### 사장님 대시보드에서 가능한 작업
1. **가게 선택**: 드롭다운에서 관리할 가게 선택
2. **영업 상태 토글**: 스위치로 영업중/휴업중 전환
3. **가게 정보 수정**: "가게 정보 수정" 버튼 클릭
4. **가게 삭제**: "가게 삭제" 버튼으로 확인 후 삭제
5. **가게 추가**: "가게 추가" 버튼으로 새 가게 등록
6. **통계 확인**: 선택된 가게의 주문/매출 통계 확인

### 수정/등록 완료 후 동작
- 가게 등록 완료 → 자동으로 `/owner/dashboard`로 이동
- 가게 정보 수정 완료 → 자동으로 `/owner/dashboard`로 이동

### 카테고리 선택 문제 해결
- 가게 수정 시 기존 선택된 카테고리가 올바르게 로드됨
- `store.category?.id || store.categoryId || null` 로직으로 안전하게 처리

## 🎨 UI 스타일
- 요기요 스타일의 빨간색 테마 (#ff1744)
- 영업중: 초록색 아이콘 및 텍스트
- 휴업중: 빨간색 아이콘 및 텍스트
- 반응형 디자인 지원 