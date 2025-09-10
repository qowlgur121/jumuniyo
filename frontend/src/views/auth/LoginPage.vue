<template>
  <ion-page>
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="login-container responsive-container">
        <!-- 헤더 영역 -->
        <div class="header-section">
          <ion-button 
            fill="clear" 
            class="back-button" 
            @click="$router.go(-1)"
          >
            <ion-icon :icon="chevronBackOutline" size="large"></ion-icon>
          </ion-button>
        </div>

        <!-- 로고 섹션 -->
        <div class="logo-section spacing-xl">
          <h1 class="app-logo responsive-title">주문이요</h1>
          <p class="welcome-message responsive-subtitle">로그인하고 다양한 혜택을 받아보세요!</p>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleLogin">
            <!-- 이메일 주소 -->
            <div class="input-group spacing-sm">
              <ion-input
                type="email"
                v-model="formData.email"
                name="email"
                placeholder="이메일 주소 입력"
                class="custom-input responsive-input"
                @ionInput="validateField('email')"
                :class="{ 'input-error': errors.email }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.email">
                {{ errors.email }}
              </ion-text>
            </div>

            <!-- 비밀번호 -->
            <div class="input-group spacing-sm">
              <ion-input
                type="password"
                v-model="formData.password"
                name="password"
                placeholder="비밀번호 입력"
                class="custom-input responsive-input"
                @ionInput="validateField('password')"
                :class="{ 'input-error': errors.password }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.password">
                {{ errors.password }}
              </ion-text>
            </div>

            <!-- 로그인 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="login-button responsive-button spacing-md"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>로그인</span>
            </ion-button>
          </form>

          <!-- 하단 링크들 -->
          <div class="auth-links spacing-md">
            <span class="link-item responsive-small" @click="goToSignUp">이메일 회원가입</span>
            <span class="divider responsive-small">|</span>
            <span class="link-item responsive-small" @click="goToFindEmail">이메일 찾기</span>
            <span class="divider responsive-small">|</span>
            <span class="link-item responsive-small" @click="goToFindPassword">비밀번호 찾기</span>
          </div>
        </div>

        <!-- 소셜 로그인 섹션 -->
        <div class="social-login-section spacing-lg">
          <div class="social-divider spacing-md">
            <span class="divider-text responsive-small">또는</span>
          </div>

          <div class="social-buttons">
            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button kakao-button responsive-button"
              @click="handleSocialLogin('kakao')"
            >
              <ion-icon :icon="chatbubbleOutline" slot="start"></ion-icon>
              카카오로 로그인
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button naver-button responsive-button"
              @click="handleSocialLogin('naver')"
            >
              <span class="naver-icon" slot="start">N</span>
              네이버로 로그인
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button apple-button responsive-button"
              @click="handleSocialLogin('apple')"
            >
              <ion-icon :icon="logoApple" slot="start"></ion-icon>
              Apple로 로그인
            </ion-button>
          </div>

          <!-- 사장님 로그인 섹션 -->
          <div class="owner-login-section spacing-lg">
            <div class="owner-divider spacing-md">
              <span class="divider-text responsive-small">사장님이신가요?</span>
            </div>
            
            <ion-button 
              expand="block" 
              fill="solid" 
              class="owner-login-button responsive-button"
              @click="goToOwnerLogin"
            >
              <ion-icon :icon="businessOutline" slot="start"></ion-icon>
              사장님 로그인
            </ion-button>
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
  chevronBackOutline, 
  chatbubbleOutline, 
  logoApple, 
  mailOutline,
  businessOutline
} from 'ionicons/icons';
import {
  IonPage,
  IonContent,
  IonInput,
  IonButton,
  IonSpinner,
  IonText,
  IonIcon,
  toastController,
  alertController,
} from '@ionic/vue';
import apiClient from '@/services/api';

const router = useRouter();
const authStore = useAuthStore();

const formData = reactive({
  email: '',
  password: '',
});

const errors = reactive({
  email: '',
  password: '',
});

const isSubmitting = computed(() => authStore.isLoading);

const validateField = (fieldName) => {
  errors[fieldName] = '';
  switch (fieldName) {
    case 'email':
      if (!formData.email) errors.email = '이메일을 입력해주세요.';
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) errors.email = '유효한 이메일 주소를 입력해주세요.';
      break;
    case 'password':
      if (!formData.password) errors.password = '비밀번호를 입력해주세요.';
      break;
  }
};

const isFormValid = computed(() => {
  return formData.email && formData.password && !errors.email && !errors.password;
});

const handleLogin = async () => {
  if (!formData.email || !formData.password) {
    const alert = await alertController.create({
      header: '입력 오류',
      message: '이메일과 비밀번호를 모두 입력해주세요.',
      buttons: ['확인'],
    });
    await alert.present();
    return;
  }

  const result = await authStore.login({
    email: formData.email,
    password: formData.password,
  });

  if (result.success) {
    const toast = await toastController.create({
      message: '로그인에 성공했습니다!',
      duration: 2000,
      color: 'success',
      position: 'top',
    });
    await toast.present();
    
    // 메인 페이지로 이동
    router.push('/');
  } else {
    const alert = await alertController.create({
      header: '로그인 실패',
      message: result.error,
      buttons: ['확인'],
    });
    await alert.present();
  }
};

const handleSocialLogin = async (provider) => {
  console.log(`${provider} 로그인 시도`);
  
  // 개발 환경에서 OAuth2 클라이언트 설정 확인
  const alert = await alertController.create({
    header: '소셜 로그인 준비 중',
    message: `${provider} 로그인 기능은 현재 개발 환경에서 OAuth2 클라이언트 설정이 완료되지 않았습니다.\n\n실제 서비스에서는 각 소셜 서비스의 개발자 콘솔에서 클라이언트 ID와 시크릿을 발급받아 설정해야 합니다.\n\n지금은 이메일 로그인을 이용해주세요.`,
    buttons: [
      {
        text: '확인',
        role: 'confirm'
      },
      {
        text: '이메일 로그인',
        handler: () => {
          // 이메일 입력 필드로 포커스 이동
          const emailInput = document.querySelector('ion-input[name="email"]');
          if (emailInput) {
            emailInput.setFocus();
          }
        }
      }
    ]
  });
  await alert.present();
  
  // 실제 OAuth2 인증 URL로 리다이렉트 (주석 처리)
  // const oauth2Url = `http://localhost:8081/oauth2/authorization/${provider}`;
  // window.location.href = oauth2Url;
};

const goToSignUp = () => {
  router.push('/auth/signup');
};

const goToFindEmail = () => {
  router.push('/auth/find-email');
};

const goToFindPassword = () => {
  router.push('/auth/find-password');
};

const goToOwnerLogin = () => {
  router.push('/owner/login');
};
</script>

<style scoped>
/* 전체 컨테이너 */
.login-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--background-white);
}

/* 헤더 영역 */
.header-section {
  display: flex;
  align-items: center;
  padding: 16px 0;
  position: relative;
  min-height: 60px;
}

.back-button {
  position: absolute;
  left: -8px;
  --color: var(--text-primary);
  --background: transparent;
  z-index: 10;
}

/* 로고 섹션 */
.logo-section {
  text-align: center;
  padding: 60px 0 40px 0;
}

.app-logo {
  font-weight: bold;
  color: var(--primary-red);
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}

.welcome-message {
  color: var(--text-secondary);
  margin: 0;
}

/* 폼 섹션 */
.form-section {
  flex: 1;
}

.input-group {
  width: 100%;
}

.custom-input {
  --background: var(--background-light);
  --border-radius: 8px;
  --padding-start: 16px;
  --padding-end: 16px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  --color: var(--text-primary);
  --placeholder-color: var(--text-placeholder);
  --placeholder-opacity: 1;
  border: 1px solid var(--border-gray);
  border-radius: 8px;
  background: var(--background-light);
  width: 100%;
}

.custom-input.input-error {
  --background: #fff8f8;
  border-color: #dc3545;
}

.error-message {
  color: #dc3545;
  margin-top: 8px;
  margin-left: 4px;
  display: block;
}

/* 로그인 버튼 */
.login-button {
  --background: var(--primary-red);
  --background-activated: var(--secondary-red);
  --background-hover: var(--secondary-red);
  --border-radius: 8px;
  --color: white;
  font-weight: 600;
  --box-shadow: none;
  width: 100%;
  margin-top: 8px;
}

.login-button:disabled {
  --background: var(--border-gray);
  --color: var(--text-placeholder);
}

/* 하단 링크들 */
.auth-links {
  text-align: center;
  color: var(--text-secondary);
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.link-item {
  color: var(--text-secondary);
  text-decoration: none;
  cursor: pointer;
  padding: 8px;
  transition: color 0.2s ease;
}

.link-item:hover {
  color: var(--primary-red);
}

.divider {
  color: #ccc;
  margin: 0 4px;
}

/* 소셜 로그인 섹션 */
.social-login-section {
  padding-bottom: 40px;
}

.social-divider {
  text-align: center;
  position: relative;
  margin: 24px 0;
}

.social-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--border-gray);
}

.divider-text {
  background-color: var(--background-white);
  padding: 0 16px;
  color: var(--text-placeholder);
}

.social-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-button {
  font-weight: 500;
  --border-radius: 8px;
  --border-color: var(--border-gray);
  --color: var(--text-primary);
  --background: var(--background-white);
  width: 100%;
}

.kakao-button {
  --background: #fee500;
  --color: #3c1e1e;
  --border-color: #fee500;
}

.naver-button {
  --background: #03c75a;
  --color: white;
  --border-color: #03c75a;
}

.naver-icon {
  background: white;
  color: #03c75a;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.apple-button {
  --background: black;
  --color: white;
  --border-color: black;
}

/* 사장님 로그인 섹션 */
.owner-login-section {
  margin-top: 20px;
}

.owner-divider {
  text-align: center;
  position: relative;
  margin: 20px 0;
}

.owner-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: var(--border-gray);
}

.owner-login-button {
  --background: var(--yogiyo-primary, #ff1744);
  --color: white;
  --border-color: var(--yogiyo-primary, #ff1744);
  font-weight: 600;
  --border-radius: 8px;
}

.owner-login-button:hover {
  --background: var(--yogiyo-primary-dark, #d50000);
  --border-color: var(--yogiyo-primary-dark, #d50000);
}

/* ===========================================
   태블릿 반응형 스타일 (768px ~ 1023px)
   ========================================== */
@media (min-width: 768px) and (max-width: 1023px) {
  .logo-section {
    padding: 80px 0 60px 0;
  }
  
  .header-section {
    min-height: 70px;
  }
  
  .social-buttons {
    gap: 16px;
  }
  
  .auth-links {
    gap: 12px;
  }
  
  .link-item {
    padding: 12px;
  }
}

/* ===========================================
   데스크톱 반응형 스타일 (1024px+)
   ========================================== */
@media (min-width: 1024px) {
  .logo-section {
    padding: 100px 0 80px 0;
  }
  
  .header-section {
    min-height: 80px;
  }
  
  .social-buttons {
    gap: 16px;
  }
  
  .auth-links {
    gap: 16px;
  }
  
  .link-item {
    padding: 16px;
  }
  
  /* 데스크톱 호버 효과 */
  .link-item:hover {
    transform: translateY(-1px);
  }
  
  .social-button:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;
  }
  
  .kakao-button:hover {
    box-shadow: 0 4px 12px rgba(254, 229, 0, 0.3);
  }
  
  .naver-button:hover {
    box-shadow: 0 4px 12px rgba(3, 199, 90, 0.3);
  }
  
  .apple-button:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  }
}

/* ===========================================
   작은 모바일 화면 (320px ~ 480px)
   ========================================== */
@media (max-width: 480px) {
  .logo-section {
    padding: 40px 0 30px 0;
  }
  
  .auth-links {
    flex-direction: column;
    gap: 16px;
  }
  
  .divider {
    display: none;
  }
  
  .link-item {
    padding: 16px;
    border: 1px solid var(--border-gray);
    border-radius: 8px;
    width: 100%;
    text-align: center;
  }
}

/* 안전 영역 대응 */
.safe-area-padding {
  padding-bottom: max(40px, env(safe-area-inset-bottom));
}
</style> 