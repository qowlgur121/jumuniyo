<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div class="find-password-container">
        <!-- 헤더 영역 -->
        <div class="header-section">
          <ion-button 
            fill="clear" 
            class="back-button" 
            @click="$router.go(-1)"
          >
            <ion-icon :icon="chevronBackOutline" size="large"></ion-icon>
          </ion-button>
          <h2 class="page-title">비밀번호 찾기</h2>
        </div>

        <!-- 안내 메시지 -->
        <div class="info-section">
          <p class="info-text">
            가입 시 등록한 이메일 주소를 입력하시면<br>
            비밀번호 재설정 링크를 보내드립니다.
          </p>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleFindPassword">
            <!-- 이메일 입력 -->
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

            <!-- 찾기 버튼 -->
            <ion-button 
              type="submit" 
              expand="block" 
              class="find-button"
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent" color="light"></ion-spinner>
              <span v-else>비밀번호 재설정 링크 전송</span>
            </ion-button>
          </form>
        </div>

        <!-- 성공 메시지 섹션 -->
        <div class="success-section" v-if="isEmailSent">
          <div class="success-card">
            <ion-icon :icon="checkmarkCircleOutline" class="success-icon"></ion-icon>
            <h3 class="success-title">재설정 링크가 전송되었습니다!</h3>
            <p class="success-message">
              입력하신 이메일 주소로 비밀번호 재설정 링크를 보내드렸습니다.<br>
              이메일을 확인하시고 링크를 클릭하여 비밀번호를 재설정해주세요.
            </p>
            <p class="notice-text">
              이메일이 도착하지 않았다면 스팸함을 확인해주세요.
            </p>
            <ion-button 
              expand="block" 
              fill="outline" 
              class="login-button"
              @click="goToLogin"
            >
              로그인하러 가기
            </ion-button>
          </div>
        </div>

        <!-- 하단 링크들 -->
        <div class="bottom-links">
          <span class="link-item" @click="goToFindEmail">이메일 찾기</span>
          <span class="divider">|</span>
          <span class="link-item" @click="goToSignUp">회원가입</span>
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
  checkmarkCircleOutline
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

const isEmailSent = ref(false);

const formData = reactive({
  email: '',
});

const errors = reactive({
  email: '',
});

const isSubmitting = ref(false);

const validateField = (fieldName) => {
  errors[fieldName] = '';
  switch (fieldName) {
    case 'email':
      if (!formData.email) {
        errors.email = '이메일을 입력해주세요.';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        errors.email = '올바른 이메일 주소를 입력해주세요.';
      }
      break;
  }
};

const isFormValid = computed(() => {
  return formData.email && !errors.email;
});

const handleFindPassword = async () => {
  // 유효성 검사
  validateField('email');
  if (errors.email) return;

  isSubmitting.value = true;

  try {
    const payload = {
      email: formData.email,
    };

    const response = await apiClient.post('/auth/reset-password', payload);

    if (response.status === 200) {
      isEmailSent.value = true;
      
      const toast = await toastController.create({
        message: '재설정 링크가 이메일로 전송되었습니다!',
        duration: 3000,
        color: 'success',
        position: 'top',
      });
      await toast.present();
    }
  } catch (error) {
    console.error('비밀번호 재설정 요청 실패:', error);
    
    // 보안상 이유로 항상 성공 메시지를 보여줌 (백엔드와 동일)
    isEmailSent.value = true;
    
    const toast = await toastController.create({
      message: '재설정 링크가 이메일로 전송되었습니다!',
      duration: 3000,
      color: 'success',
      position: 'top',
    });
    await toast.present();
  } finally {
    isSubmitting.value = false;
  }
};

const goToLogin = () => {
  router.push('/auth/login');
};

const goToFindEmail = () => {
  router.push('/auth/find-email');
};

const goToSignUp = () => {
  router.push('/auth/signup');
};
</script>

<style scoped>
/* 전체 컨테이너 */
.find-password-container {
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

/* 안내 섹션 */
.info-section {
  text-align: center;
  padding: 0 0 40px 0;
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

/* 찾기 버튼 */
.find-button {
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

.find-button:disabled {
  --background: #e9ecef;
  --color: #999;
}

/* 성공 메시지 섹션 */
.success-section {
  margin-top: 32px;
}

.success-card {
  background: #f8fff8;
  border: 1px solid #d4edda;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
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
  margin: 0 0 16px 0;
}

.notice-text {
  font-size: 12px;
  color: #999;
  margin: 0 0 24px 0;
}

.login-button {
  --background: transparent;
  --background-activated: #f8f9fa;
  --background-hover: #f8f9fa;
  --border-color: #ff1744;
  --color: #ff1744;
  --border-radius: 8px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 하단 링크들 */
.bottom-links {
  text-align: center;
  padding: 32px 0 40px 0;
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
</style> 