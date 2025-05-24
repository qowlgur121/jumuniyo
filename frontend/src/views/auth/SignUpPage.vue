<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div class="signup-container">
        <!-- 헤더 영역 -->
        <div class="header-section">
          <ion-button 
            fill="clear" 
            class="back-button" 
            @click="$router.go(-1)"
          >
            <ion-icon :icon="chevronBackOutline" size="large"></ion-icon>
          </ion-button>
          <h1 class="page-title">회원가입</h1>
        </div>

        <!-- 로고 섹션 -->
        <div class="logo-section">
          <h1 class="app-logo">주문이요</h1>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleSignUp">
            <!-- 이메일 주소 -->
            <div class="input-group">
              <ion-input
                type="email"
                v-model="formData.email"
                name="email"
                placeholder="이메일 주소 입력"
                class="custom-input"
                @ionInput="validateField('email')"
                :class="{ 'input-error': errors.email, 'input-valid': !errors.email && formData.email }"
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
                placeholder="영문, 숫자, 특수문자 포함 8자리 이상"
                class="custom-input"
                @ionInput="validateField('password')"
                :class="{ 'input-error': errors.password, 'input-valid': !errors.password && formData.password }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.password">
                {{ errors.password }}
              </ion-text>
            </div>

            <!-- 비밀번호 확인 -->
            <div class="input-group">
              <ion-input
                type="password"
                v-model="formData.passwordConfirm"
                name="passwordConfirm"
                placeholder="비밀번호 재입력"
                class="custom-input"
                @ionInput="validateField('passwordConfirm')"
                :class="{ 'input-error': errors.passwordConfirm, 'input-valid': !errors.passwordConfirm && formData.passwordConfirm }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.passwordConfirm">
                {{ errors.passwordConfirm }}
              </ion-text>
            </div>

            <!-- 닉네임 -->
            <div class="input-group">
              <ion-input
                type="text"
                v-model="formData.nickname"
                name="nickname"
                placeholder="닉네임"
                class="custom-input"
                @ionInput="validateField('nickname')"
                :class="{ 'input-error': errors.nickname, 'input-valid': !errors.nickname && formData.nickname }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.nickname">
                {{ errors.nickname }}
              </ion-text>
            </div>

            <!-- 전화번호 -->
            <div class="input-group">
              <ion-input
                type="tel"
                v-model="formData.phoneNumber"
                name="phoneNumber"
                placeholder="전화번호 (예: 010-1234-5678)"
                class="custom-input"
                @ionInput="validateField('phoneNumber')"
                :class="{ 'input-error': errors.phoneNumber, 'input-valid': !errors.phoneNumber && formData.phoneNumber }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.phoneNumber">
                {{ errors.phoneNumber }}
              </ion-text>
            </div>

            <!-- 가입하기 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="signup-button"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>다음</span>
            </ion-button>
          </form>
        </div>

        <!-- 하단 링크들 -->
        <div class="bottom-links">
          <span class="link-item" @click="goToLogin">이미 계정이 있나요? 로그인</span>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { chevronBackOutline } from 'ionicons/icons';
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
  passwordConfirm: '',
  nickname: '',
  phoneNumber: '',
});

const errors = reactive({
  email: '',
  password: '',
  passwordConfirm: '',
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
    case 'passwordConfirm':
      if (!formData.passwordConfirm) errors.passwordConfirm = '비밀번호 확인을 입력해주세요.';
      else if (formData.password !== formData.passwordConfirm) errors.passwordConfirm = '비밀번호가 일치하지 않습니다.';
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
  validateField('passwordConfirm');
  validateField('nickname');
  validateField('phoneNumber');
  return !Object.values(errors).some(error => error !== '');
};

const isFormValid = computed(() => {
  return formData.email && formData.password && formData.passwordConfirm && formData.nickname && formData.phoneNumber &&
         !errors.email && !errors.password && !errors.passwordConfirm && !errors.nickname && !errors.phoneNumber;
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
</script>

<style scoped>
/* 전체 컨테이너 */
.signup-container {
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

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
  text-align: center;
  flex: 1;
}

/* 로고 섹션 */
.logo-section {
  text-align: center;
  padding: 60px 0 80px 0;
}

.app-logo {
  font-size: 48px;
  font-weight: bold;
  color: #ff1744;
  margin: 0;
  letter-spacing: -1px;
}

/* 폼 섹션 */
.form-section {
  flex: 1;
  display: flex;
  flex-direction: column;
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

.custom-input.input-valid {
  --background: #f8fff8;
  border-color: #28a745;
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

/* 가입하기 버튼 */
.signup-button {
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

.signup-button:disabled {
  --background: #e9ecef;
  --color: #999;
}

/* 하단 링크들 */
.bottom-links {
  text-align: center;
  padding: 40px 0;
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

/* 반응형 디자인 */
@media (max-width: 480px) {
  .signup-container {
    padding: 0 20px;
  }
  
  .app-logo {
    font-size: 40px;
  }
  
  .logo-section {
    padding: 40px 0 60px 0;
  }
}
</style>