<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/auth/login"></ion-back-button>
        </ion-buttons>
        <ion-title>사장님 로그인</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="owner-login-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="login-header">
          <div class="logo-section">
            <div class="logo">
              <span class="logo-text">주문이요</span>
              <span class="owner-badge">사장님</span>
            </div>
            <h1 class="responsive-title">사장님 로그인</h1>
            <p class="subtitle">우리 가게 관리를 시작해보세요!</p>
          </div>
        </div>

        <!-- 로그인 폼 -->
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-section">
            <!-- 이메일 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.email"
                type="email"
                placeholder="사장님 이메일을 입력하세요"
                required
                :class="{ 'ion-invalid': errors.email }"
                @ionBlur="validateField('email')"
              >
                <div slot="label">이메일</div>
              </ion-input>
            </ion-item>
            <div v-if="errors.email" class="error-message">{{ errors.email }}</div>

            <!-- 비밀번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.password"
                type="password"
                placeholder="비밀번호를 입력하세요"
                required
                :class="{ 'ion-invalid': errors.password }"
                @ionBlur="validateField('password')"
              >
                <div slot="label">비밀번호</div>
              </ion-input>
            </ion-item>
            <div v-if="errors.password" class="error-message">{{ errors.password }}</div>

            <!-- 로그인 버튼 -->
            <ion-button
              type="submit"
              expand="block"
              size="large"
              class="login-button"
              :disabled="isLoading || !isFormValid"
            >
              <ion-spinner v-if="isLoading" name="crescent"></ion-spinner>
              <span v-else>사장님 로그인</span>
            </ion-button>
          </div>
        </form>

        <!-- 링크 섹션 -->
        <div class="links-section">
          <div class="auth-links">
            <span @click="goToFindEmail" class="link">이메일 찾기</span>
            <span class="divider">|</span>
            <span @click="goToFindPassword" class="link">비밀번호 찾기</span>
          </div>
        </div>

        <!-- 하단 링크 -->
        <div class="bottom-links">
          <p>아직 사장님 계정이 없나요?</p>
          <ion-button
            expand="block"
            fill="outline"
            size="large"
            class="signup-button"
            @click="goToOwnerSignUp"
          >
            사장님 회원가입
          </ion-button>
          
          <div class="customer-link">
            <p>일반 고객이신가요? <span @click="goToCustomerLogin" class="link">고객 로그인</span></p>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonItem,
  IonInput,
  IonButton,
  IonSpinner,
  toastController
} from '@ionic/vue';
import apiClient from '@/services/api.js';

const router = useRouter();
const authStore = useAuthStore();

// 상태 관리
const isLoading = ref(false);

// 폼 데이터
const formData = reactive({
  email: '',
  password: ''
});

// 에러 상태
const errors = reactive({
  email: '',
  password: ''
});

// 폼 유효성 체크
const isFormValid = computed(() => {
  return formData.email && formData.password && !Object.values(errors).some(error => error);
});

// 필드별 유효성 검증
const validateField = (fieldName) => {
  errors[fieldName] = '';

  switch (fieldName) {
    case 'email':
      if (!formData.email) {
        errors.email = '이메일은 필수 입력 값입니다.';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        errors.email = '올바른 이메일 형식이 아닙니다.';
      }
      break;

    case 'password':
      if (!formData.password) {
        errors.password = '비밀번호는 필수 입력 값입니다.';
      }
      break;
  }
};

// 로그인 처리
const handleLogin = async () => {
  // 모든 필드 유효성 검증
  Object.keys(errors).forEach(field => validateField(field));

  if (!isFormValid.value) {
    showToast('입력 정보를 확인해주세요.', 'warning');
    return;
  }

  isLoading.value = true;

  try {
    const response = await apiClient.post('/auth/login', formData);
    const { token, tokenType, userId, email, nickname, role, status, profileImageUrl } = response.data;

    // 사장님 역할 확인
    if (role !== 'ROLE_OWNER') {
      showToast('사장님 계정이 아닙니다. 사장님 전용 계정으로 로그인해주세요.', 'warning');
      return;
    }

    // 승인 상태 확인
    if (status === 'PENDING_APPROVAL') {
      showToast('아직 승인 대기 중인 계정입니다. 승인 후 이용해주세요.', 'warning');
      return;
    }

    // 사용자 객체 생성
    const user = {
      userId,
      email,
      nickname,
      role,
      status,
      profileImageUrl
    };

    // 토큰과 사용자 정보 저장
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    
    // Pinia 스토어에 저장
    authStore.setUser(user);
    authStore.setToken(token);

    showToast('로그인 성공!', 'success');
    
    // 사장님 전용 대시보드로 이동
    router.push('/owner/dashboard');

  } catch (error) {
    console.error('로그인 실패:', error);
    
    let errorMessage = '로그인 중 오류가 발생했습니다.';
    if (error.response?.status === 401) {
      errorMessage = '이메일 또는 비밀번호가 올바르지 않습니다.';
    } else if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    showToast(errorMessage, 'danger');
  } finally {
    isLoading.value = false;
  }
};

// 페이지 이동 함수들
const goToFindEmail = () => {
  router.push('/auth/find-email');
};

const goToFindPassword = () => {
  router.push('/auth/find-password');
};

const goToOwnerSignUp = () => {
  router.push('/owner/signup');
};

const goToCustomerLogin = () => {
  router.push('/auth/login');
};

// 토스트 메시지 표시
const showToast = async (message, color = 'primary') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    color,
    position: 'top'
  });
  await toast.present();
};
</script>

<style scoped>
.owner-login-content {
  --background: var(--ion-color-light);
}

.login-header {
  background: linear-gradient(135deg, #ff1744 0%, #e91e63 100%);
  color: white;
  padding: 3rem 1rem 4rem;
  text-align: center;
}

.logo-section {
  max-width: 400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
}

.logo-text {
  font-size: 2.5rem;
  font-weight: 900;
  color: white;
}

.owner-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
}

.login-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.8rem;
  font-weight: 700;
  color: white;
}

.subtitle {
  margin: 0;
  font-size: 1.1rem;
  opacity: 0.9;
  color: white;
}

.login-form {
  max-width: 400px;
  margin: -3rem auto 0;
  padding: 0 1rem;
}

.form-section {
  background: white;
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.form-item {
  margin-bottom: 1.5rem;
  --border-color: #e0e0e0;
  --highlight-color: #ff1744;
}

.form-item:last-of-type {
  margin-bottom: 2rem;
}

.error-message {
  color: #dc3545;
  font-size: 0.875rem;
  margin: 0.25rem 0 0.5rem 1rem;
}

.login-button {
  --background: #ff1744;
  --background-hover: #d50000;
  --color: white;
  font-weight: 600;
  height: 56px;
  margin: 0;
}

.login-button:disabled {
  --background: #cccccc;
  --color: #666666;
}

.links-section {
  max-width: 400px;
  margin: 0 auto 2rem;
  padding: 0 1rem;
}

.auth-links {
  text-align: center;
  margin-bottom: 1rem;
}

.link {
  color: #ff1744;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.9rem;
}

.link:hover {
  text-decoration: underline;
  color: #d50000;
}

.divider {
  color: #666666;
  margin: 0 1rem;
}

.bottom-links {
  max-width: 400px;
  margin: 0 auto;
  padding: 0 1rem 2rem;
  text-align: center;
}

.bottom-links p {
  color: #333333;
  font-size: 1rem;
  margin-bottom: 1rem;
}

.signup-button {
  --border-color: #ff1744;
  --color: #ff1744;
  font-weight: 600;
  height: 48px;
  margin-bottom: 2rem;
}

.customer-link {
  padding-top: 1rem;
  border-top: 1px solid #e0e0e0;
}

.customer-link p {
  margin: 0;
  font-size: 0.9rem;
  color: #666666;
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .login-header {
    padding: 2rem 1rem 3rem;
  }
  
  .login-header h1 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .logo-text {
    font-size: 2rem;
  }
  
  .form-section {
    margin: -2rem 1rem 2rem;
    padding: 1.5rem;
    border-radius: 12px;
  }
}

@media (min-width: 768px) {
  .login-form,
  .links-section,
  .bottom-links {
    padding: 0 2rem;
  }
}
</style> 