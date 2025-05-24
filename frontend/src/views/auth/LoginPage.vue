<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div class="login-container">
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
        <div class="logo-section">
          <h1 class="app-logo">주문이요</h1>
          <p class="welcome-message">로그인하고 다양한 혜택을 받아보세요!</p>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleLogin">
            <!-- 이메일 주소 -->
            <div class="input-group">
              <ion-input
                type="email"
                v-model="formData.email"
                name="email"
                placeholder="이메일 주소 입력"
                class="custom-input"
                @ionInput="validateField('email')"
                :class="{ 'input-error': errors.email }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.email">
                {{ errors.email }}
              </ion-text>
            </div>

            <!-- 비밀번호 -->
            <div class="input-group">
              <ion-input
                type="password"
                v-model="formData.password"
                name="password"
                placeholder="비밀번호 입력"
                class="custom-input"
                @ionInput="validateField('password')"
                :class="{ 'input-error': errors.password }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.password">
                {{ errors.password }}
              </ion-text>
            </div>

            <!-- 로그인 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="login-button"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>로그인</span>
            </ion-button>
          </form>

          <!-- 하단 링크들 -->
          <div class="auth-links">
            <span class="link-item" @click="goToSignUp">이메일 회원가입</span>
            <span class="divider">|</span>
            <span class="link-item" @click="goToFindEmail">이메일 찾기</span>
            <span class="divider">|</span>
            <span class="link-item" @click="goToFindPassword">비밀번호 찾기</span>
          </div>
        </div>

        <!-- 소셜 로그인 섹션 -->
        <div class="social-login-section">
          <div class="social-divider">
            <span class="divider-text">또는</span>
          </div>

          <div class="social-buttons">
            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button kakao-button"
              @click="handleSocialLogin('kakao')"
            >
              <ion-icon :icon="chatbubbleOutline" slot="start"></ion-icon>
              카카오로 로그인
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button naver-button"
              @click="handleSocialLogin('naver')"
            >
              <span class="naver-icon" slot="start">N</span>
              네이버로 로그인
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button apple-button"
              @click="handleSocialLogin('apple')"
            >
              <ion-icon :icon="logoApple" slot="start"></ion-icon>
              Apple로 로그인
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
  mailOutline 
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
</script>

<style scoped>
/* 전체 컨테이너 */
.login-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #ffffff;
  padding: 0 24px;
}

/* 헤더 영역 */
.header-section {
  display: flex;
  align-items: center;
  padding: 16px 0;
  position: relative;
}

.back-button {
  position: absolute;
  left: -8px;
  --color: #333;
  --background: transparent;
}

/* 로고 섹션 */
.logo-section {
  text-align: center;
  padding: 100px 0 80px 0;
}

.app-logo {
  font-size: 48px;
  font-weight: bold;
  color: #ff1744;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}

.welcome-message {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 폼 섹션 */
.form-section {
  flex: 1;
}

.input-group {
  margin-bottom: 16px;
}

.custom-input {
  --background: #f8f9fa;
  --border-radius: 8px;
  --padding-start: 16px;
  --padding-end: 16px;
  --padding-top: 16px;
  --padding-bottom: 16px;
  --color: #333;
  --placeholder-color: #999;
  --placeholder-opacity: 1;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 16px;
  height: 56px;
  background: #f8f9fa;
}

.custom-input.input-error {
  --background: #fff8f8;
  border-color: #dc3545;
}

.error-message {
  font-size: 14px;
  margin-top: 8px;
  margin-left: 4px;
  display: block;
}

/* 로그인 버튼 */
.login-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --background-hover: #e50032;
  --border-radius: 8px;
  --color: white;
  height: 56px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 24px;
  --box-shadow: none;
}

.login-button:disabled {
  --background: #e9ecef;
  --color: #999;
}

/* 하단 링크들 */
.auth-links {
  text-align: center;
  padding: 24px 0;
  font-size: 14px;
  color: #666;
}

.link-item {
  color: #666;
  text-decoration: none;
  cursor: pointer;
  padding: 8px;
}

.link-item:hover {
  color: #ff1744;
}

.divider {
  margin: 0 8px;
  color: #ccc;
}

/* 소셜 로그인 섹션 */
.social-login-section {
  padding-bottom: 40px;
}

.social-divider {
  text-align: center;
  margin: 32px 0;
  position: relative;
}

.social-divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #e9ecef;
}

.divider-text {
  background-color: white;
  padding: 0 16px;
  color: #999;
  font-size: 14px;
}

.social-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-button {
  height: 56px;
  font-size: 16px;
  font-weight: 500;
  --border-radius: 8px;
  --border-color: #e9ecef;
  --color: #333;
  --background: white;
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

/* 반응형 디자인 */
@media (max-width: 480px) {
  .login-container {
    padding: 0 20px;
  }
  
  .app-logo {
    font-size: 40px;
  }
  
  .logo-section {
    padding: 80px 0 60px 0;
  }
}
</style> 