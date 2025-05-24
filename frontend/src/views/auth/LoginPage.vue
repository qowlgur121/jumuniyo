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
            <span class="link-item" @click="goToFindEmail">이메일 회원가입</span>
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

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button email-button"
              @click="goToSignUp"
            >
              <ion-icon :icon="mailOutline" slot="start"></ion-icon>
              이메일로 로그인
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

const formData = reactive({
  email: '',
  password: '',
});

const errors = reactive({
  email: '',
  password: '',
});

const isSubmitting = ref(false);

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

  isSubmitting.value = true;
  try {
    const payload = {
      email: formData.email,
      password: formData.password,
    };
    const response = await apiClient.post('/auth/login', payload);

    if (response.status === 200) {
      const toast = await toastController.create({
        message: '로그인에 성공했습니다!',
        duration: 2000,
        color: 'success',
        position: 'top',
      });
      await toast.present();
      
      // 토큰 저장 및 메인 페이지로 이동
      localStorage.setItem('token', response.data.token);
      router.push('/');
    }
  } catch (error) {
    console.error('로그인 실패:', error);
    let errorMessage = '로그인 중 오류가 발생했습니다.';
    if (error.response && error.response.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data;
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message;
      }
    }

    const alert = await alertController.create({
      header: '로그인 실패',
      message: errorMessage,
      buttons: ['확인'],
    });
    await alert.present();
  } finally {
    isSubmitting.value = false;
  }
};

const handleSocialLogin = (provider) => {
  // TODO: 소셜 로그인 구현
  console.log(`${provider} 로그인 시도`);
  // 실제로는 OAuth2 URL로 리다이렉트 또는 팝업 창 열기
};

const goToSignUp = () => {
  router.push('/auth/signup');
};

const goToFindEmail = () => {
  // TODO: 이메일 찾기 페이지로 이동
  console.log('이메일 찾기');
};

const goToFindPassword = () => {
  // TODO: 비밀번호 찾기 페이지로 이동
  console.log('비밀번호 찾기');
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

.email-button {
  --border-color: #ff1744;
  --color: #ff1744;
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