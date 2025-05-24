<template>
  <ion-page>
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="signup-container responsive-container">
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
        <div class="logo-section spacing-lg">
          <h1 class="app-logo responsive-title">주문이요</h1>
          <p class="welcome-message responsive-subtitle">맛있는 음식을 주문하고 즐기세요!</p>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleSignUp">
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
                placeholder="비밀번호 입력 (8자 이상)"
                class="custom-input responsive-input"
                @ionInput="validateField('password')"
                :class="{ 'input-error': errors.password }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.password">
                {{ errors.password }}
              </ion-text>
            </div>

            <!-- 비밀번호 확인 -->
            <div class="input-group spacing-sm">
              <ion-input
                type="password"
                v-model="formData.confirmPassword"
                name="confirmPassword"
                placeholder="비밀번호 확인"
                class="custom-input responsive-input"
                @ionInput="validateField('confirmPassword')"
                :class="{ 'input-error': errors.confirmPassword }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.confirmPassword">
                {{ errors.confirmPassword }}
              </ion-text>
            </div>

            <!-- 닉네임 -->
            <div class="input-group spacing-sm">
              <ion-input
                type="text"
                v-model="formData.nickname"
                name="nickname"
                placeholder="닉네임 입력"
                class="custom-input responsive-input"
                @ionInput="validateField('nickname')"
                :class="{ 'input-error': errors.nickname }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.nickname">
                {{ errors.nickname }}
              </ion-text>
            </div>

            <!-- 전화번호 -->
            <div class="input-group spacing-sm">
              <ion-input
                type="tel"
                v-model="formData.phoneNumber"
                name="phoneNumber"
                placeholder="전화번호 입력 (010-1234-5678)"
                class="custom-input responsive-input"
                @ionInput="validateField('phoneNumber')"
                :class="{ 'input-error': errors.phoneNumber }"
              ></ion-input>
              <ion-text color="danger" class="error-message responsive-small" v-if="errors.phoneNumber">
                {{ errors.phoneNumber }}
              </ion-text>
            </div>

            <!-- 회원가입 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="signup-button responsive-button spacing-md"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>회원가입</span>
            </ion-button>
          </form>

          <!-- 하단 링크들 -->
          <div class="auth-links spacing-md">
            <span class="responsive-small">이미 계정이 있나요?</span>
            <span class="link-item responsive-small" @click="goToLogin">로그인</span>
          </div>
        </div>

        <!-- 소셜 로그인 섹션 -->
        <div class="social-login-section spacing-lg">
          <div class="social-divider spacing-md">
            <span class="divider-text responsive-small"></span>
          </div>

          <div class="social-buttons">
            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button kakao-button responsive-button"
              @click="handleSocialLogin('kakao')"
            >
              <ion-icon :icon="chatbubbleOutline" slot="start"></ion-icon>
              카카오로 가입
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button naver-button responsive-button"
              @click="handleSocialLogin('naver')"
            >
              <span class="naver-icon" slot="start">N</span>
              네이버로 가입
            </ion-button>

            <ion-button 
              expand="block" 
              fill="outline" 
              class="social-button apple-button responsive-button"
              @click="handleSocialLogin('apple')"
            >
              <ion-icon :icon="logoApple" slot="start"></ion-icon>
              Apple로 가입
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
import { chevronBackOutline, chatbubbleOutline, logoApple } from 'ionicons/icons';
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
  confirmPassword: '',
  nickname: '',
  phoneNumber: '',
});

const errors = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phoneNumber: '',
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
      else if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,20}$/.test(formData.password)) {
        errors.password = '8~20자 영문, 숫자, 특수문자를 모두 포함해야 합니다.';
      }
      break;
    case 'confirmPassword':
      if (!formData.confirmPassword) errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
      else if (formData.password !== formData.confirmPassword) errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      break;
    case 'nickname':
      if (!formData.nickname) errors.nickname = '닉네임을 입력해주세요.';
      else if (formData.nickname.length < 2 || formData.nickname.length > 10) errors.nickname = '닉네임은 2~10자로 입력해주세요.';
      else if (!/^[가-힣A-Za-z0-9]*$/.test(formData.nickname)) errors.nickname = '닉네임은 한글, 영문, 숫자만 사용 가능합니다.';
      break;
    case 'phoneNumber':
      if (!formData.phoneNumber) errors.phoneNumber = '전화번호를 입력해주세요.';
      else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(formData.phoneNumber)) errors.phoneNumber = '유효한 전화번호 형식을 입력해주세요. (예: 010-1234-5678)';
      break;
  }
};

const validateForm = () => {
  validateField('email');
  validateField('password');
  validateField('confirmPassword');
  validateField('nickname');
  validateField('phoneNumber');
  return !Object.values(errors).some(error => error !== '');
};

const isFormValid = computed(() => {
  return formData.email && formData.password && formData.confirmPassword && formData.nickname && formData.phoneNumber &&
         !errors.email && !errors.password && !errors.confirmPassword && !errors.nickname && !errors.phoneNumber;
});

const handleSignUp = async () => {
  if (!validateForm()) {
    const firstErrorField = Object.keys(errors).find(key => errors[key]);
    if(firstErrorField) {
      const firstErrorMessage = errors[firstErrorField];
      const alert = await alertController.create({
          header: '입력 오류',
          message: firstErrorMessage || '입력값을 확인해주세요.',
          buttons: ['확인'],
        });
      await alert.present();
    }
    return;
  }

  const result = await authStore.signup({
    email: formData.email,
    password: formData.password,
    nickname: formData.nickname,
    phoneNumber: formData.phoneNumber,
  });

  if (result.success) {
    const toast = await toastController.create({
      message: '회원가입이 완료되었습니다! 로그인해주세요.',
      duration: 3000,
      color: 'success',
      position: 'top',
    });
    await toast.present();
    router.push('/auth/login');
  } else {
    const alert = await alertController.create({
      header: '회원가입 실패',
      message: result.error,
      buttons: ['확인'],
    });
    await alert.present();
  }
};

const goToLogin = () => {
  router.push('/auth/login');
};

const handleSocialLogin = (provider) => {
  // 소셜 로그인 처리 로직을 구현해야 합니다.
  console.log(`Social login with ${provider}`);
};
</script>

<style scoped>
/* 전체 컨테이너 */
.signup-container {
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
  padding: 40px 0 30px 0;
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

/* 회원가입 버튼 */
.signup-button {
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

.signup-button:disabled {
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
  color: var(--primary-red);
  text-decoration: none;
  cursor: pointer;
  padding: 8px;
  font-weight: 600;
  transition: color 0.2s ease;
}

.link-item:hover {
  color: var(--secondary-red);
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

/* ===========================================
   태블릿 반응형 스타일 (768px ~ 1023px)
   ========================================== */
@media (min-width: 768px) and (max-width: 1023px) {
  .logo-section {
    padding: 60px 0 40px 0;
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
    padding: 80px 0 60px 0;
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
    padding: 30px 0 20px 0;
  }
  
  .auth-links {
    flex-direction: column;
    gap: 16px;
  }
  
  .link-item {
    padding: 16px;
    border: 1px solid var(--primary-red);
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