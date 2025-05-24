<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div class="reset-password-container">
        <!-- 헤더 영역 -->
        <div class="header-section">
          <ion-button 
            fill="clear" 
            class="back-button" 
            @click="goToLogin"
          >
            <ion-icon :icon="chevronBackOutline" size="large"></ion-icon>
          </ion-button>
          <h2 class="page-title">비밀번호 재설정</h2>
        </div>

        <!-- 로딩 섹션 -->
        <div class="loading-section" v-if="isValidatingToken">
          <ion-spinner name="crescent" color="primary"></ion-spinner>
          <p class="loading-text">토큰을 확인하고 있습니다...</p>
        </div>

        <!-- 유효하지 않은 토큰 섹션 -->
        <div class="invalid-token-section" v-else-if="!isTokenValid">
          <div class="error-card">
            <ion-icon :icon="alertCircleOutline" class="error-icon"></ion-icon>
            <h3 class="error-title">유효하지 않은 링크입니다</h3>
            <p class="error-message">
              비밀번호 재설정 링크가 만료되었거나 유효하지 않습니다.<br>
              새로운 재설정 링크를 요청해주세요.
            </p>
            <ion-button 
              expand="block" 
              fill="outline" 
              class="retry-button"
              @click="goToFindPassword"
            >
              비밀번호 찾기로 돌아가기
            </ion-button>
          </div>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section" v-else-if="isTokenValid && !isPasswordChanged">
          <div class="info-section">
            <p class="info-text">
              새로운 비밀번호를 입력해주세요.
            </p>
          </div>

          <form @submit.prevent="handleResetPassword">
            <!-- 새 비밀번호 입력 -->
            <div class="input-group">
              <ion-input
                type="password"
                v-model="formData.newPassword"
                name="newPassword"
                placeholder="새 비밀번호 입력 (8자 이상, 영문/숫자/특수문자 포함)"
                class="custom-input"
                @ionInput="validateField('newPassword')"
                :class="{ 'input-error': errors.newPassword }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.newPassword">
                {{ errors.newPassword }}
              </ion-text>
            </div>

            <!-- 비밀번호 확인 입력 -->
            <div class="input-group">
              <ion-input
                type="password"
                v-model="formData.confirmPassword"
                name="confirmPassword"
                placeholder="비밀번호 확인"
                class="custom-input"
                @ionInput="validateField('confirmPassword')"
                :class="{ 'input-error': errors.confirmPassword }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.confirmPassword">
                {{ errors.confirmPassword }}
              </ion-text>
            </div>

            <!-- 재설정 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="reset-button"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>비밀번호 변경</span>
            </ion-button>
          </form>
        </div>

        <!-- 성공 섹션 -->
        <div class="success-section" v-else-if="isPasswordChanged">
          <div class="success-card">
            <ion-icon :icon="checkmarkCircleOutline" class="success-icon"></ion-icon>
            <h3 class="success-title">비밀번호가 변경되었습니다!</h3>
            <p class="success-message">
              새로운 비밀번호로 로그인해주세요.
            </p>
            <ion-button 
              expand="block" 
              class="login-button"
              @click="goToLogin"
            >
              로그인하러 가기
            </ion-button>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { 
  chevronBackOutline,
  checkmarkCircleOutline,
  alertCircleOutline
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
const route = useRoute();

const isValidatingToken = ref(true);
const isTokenValid = ref(false);
const isPasswordChanged = ref(false);
const token = ref('');

const formData = reactive({
  newPassword: '',
  confirmPassword: '',
});

const errors = reactive({
  newPassword: '',
  confirmPassword: '',
});

const isSubmitting = ref(false);

const validateField = (fieldName) => {
  errors[fieldName] = '';
  switch (fieldName) {
    case 'newPassword':
      if (!formData.newPassword) {
        errors.newPassword = '새 비밀번호를 입력해주세요.';
      } else if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(formData.newPassword)) {
        errors.newPassword = '비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.';
      }
      break;
    case 'confirmPassword':
      if (!formData.confirmPassword) {
        errors.confirmPassword = '비밀번호 확인을 입력해주세요.';
      } else if (formData.newPassword !== formData.confirmPassword) {
        errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      }
      break;
  }
};

const isFormValid = computed(() => {
  return formData.newPassword && 
         formData.confirmPassword && 
         !errors.newPassword && 
         !errors.confirmPassword && 
         formData.newPassword === formData.confirmPassword;
});

const validateToken = async () => {
  try {
    token.value = route.query.token;
    
    if (!token.value) {
      isTokenValid.value = false;
      return;
    }

    const response = await apiClient.get(`/auth/reset-password/validate?token=${token.value}`);
    
    if (response.status === 200 && response.data === true) {
      isTokenValid.value = true;
    } else {
      isTokenValid.value = false;
    }
  } catch (error) {
    console.error('토큰 검증 실패:', error);
    isTokenValid.value = false;
  } finally {
    isValidatingToken.value = false;
  }
};

const handleResetPassword = async () => {
  // 유효성 검사
  validateField('newPassword');
  validateField('confirmPassword');
  
  if (errors.newPassword || errors.confirmPassword) return;

  isSubmitting.value = true;

  try {
    const payload = {
      token: token.value,
      newPassword: formData.newPassword,
      confirmPassword: formData.confirmPassword,
    };

    const response = await apiClient.post('/auth/change-password', payload);

    if (response.status === 200) {
      isPasswordChanged.value = true;
      
      const toast = await toastController.create({
        message: '비밀번호가 성공적으로 변경되었습니다!',
        duration: 3000,
        color: 'success',
        position: 'top',
      });
      await toast.present();
    }
  } catch (error) {
    console.error('비밀번호 변경 실패:', error);
    
    let errorMessage = '비밀번호 변경 중 오류가 발생했습니다.';
    if (error.response && error.response.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data;
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message;
      }
    }

    const alert = await alertController.create({
      header: '비밀번호 변경 실패',
      message: errorMessage,
      buttons: ['확인'],
    });
    await alert.present();
  } finally {
    isSubmitting.value = false;
  }
};

const goToLogin = () => {
  router.push('/auth/login');
};

const goToFindPassword = () => {
  router.push('/auth/find-password');
};

onMounted(() => {
  validateToken();
});
</script>

<style scoped>
/* 전체 컨테이너 */
.reset-password-container {
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
  padding: 16px 0 32px 0;
  position: relative;
}

.back-button {
  position: absolute;
  left: -8px;
  --color: #333;
  --background: transparent;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  text-align: center;
  width: 100%;
}

/* 로딩 섹션 */
.loading-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.loading-text {
  font-size: 16px;
  color: #666;
  margin-top: 16px;
}

/* 에러 섹션 */
.invalid-token-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-card {
  background: #fff8f8;
  border: 1px solid #f5c6cb;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.error-icon {
  font-size: 48px;
  color: #dc3545;
  margin-bottom: 16px;
}

.error-title {
  font-size: 18px;
  font-weight: 600;
  color: #721c24;
  margin: 0 0 16px 0;
}

.error-message {
  font-size: 14px;
  color: #721c24;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.retry-button {
  --background: transparent;
  --background-activated: #f8f9fa;
  --background-hover: #f8f9fa;
  --border-color: #dc3545;
  --color: #dc3545;
  --border-radius: 8px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 안내 섹션 */
.info-section {
  text-align: center;
  padding: 0 0 32px 0;
}

.info-text {
  font-size: 16px;
  color: #666;
  line-height: 1.5;
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

/* 재설정 버튼 */
.reset-button {
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

.reset-button:disabled {
  --background: #e9ecef;
  --color: #999;
}

/* 성공 섹션 */
.success-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-card {
  background: #f8fff8;
  border: 1px solid #d4edda;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.success-icon {
  font-size: 48px;
  color: #28a745;
  margin-bottom: 16px;
}

.success-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.success-message {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.login-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --background-hover: #e50032;
  --border-radius: 8px;
  --color: white;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}
</style> 