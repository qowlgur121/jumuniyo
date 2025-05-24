<template>
  <ion-page>
    <ion-content :fullscreen="true">
      <div class="find-email-container">
        <!-- 헤더 영역 -->
        <div class="header-section">
          <ion-button 
            fill="clear" 
            class="back-button" 
            @click="$router.go(-1)"
          >
            <ion-icon :icon="chevronBackOutline" size="large"></ion-icon>
          </ion-button>
          <h2 class="page-title">이메일 찾기</h2>
        </div>

        <!-- 안내 메시지 -->
        <div class="info-section">
          <p class="info-text">
            가입 시 등록한 닉네임과 전화번호를 입력하시면<br>
            등록된 이메일 주소를 확인할 수 있습니다.
          </p>
        </div>

        <!-- 폼 섹션 -->
        <div class="form-section">
          <form @submit.prevent="handleFindEmail">
            <!-- 닉네임 입력 -->
            <div class="input-group">
              <ion-input
                type="text"
                v-model="formData.nickname"
                name="nickname"
                placeholder="닉네임 입력"
                class="custom-input"
                @ionInput="validateField('nickname')"
                :class="{ 'input-error': errors.nickname }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.nickname">
                {{ errors.nickname }}
              </ion-text>
            </div>

            <!-- 전화번호 입력 -->
            <div class="input-group">
              <ion-input
                type="tel"
                v-model="formData.phoneNumber"
                name="phoneNumber"
                placeholder="전화번호 입력 (예: 010-1234-5678)"
                class="custom-input"
                @ionInput="validateField('phoneNumber')"
                :class="{ 'input-error': errors.phoneNumber }"
              ></ion-input>
              <ion-text color="danger" class="error-message" v-if="errors.phoneNumber">
                {{ errors.phoneNumber }}
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
              <span v-else>이메일 찾기</span>
            </ion-button>
          </form>
        </div>

        <!-- 결과 섹션 -->
        <div class="result-section" v-if="foundEmail">
          <div class="result-card">
            <ion-icon :icon="checkmarkCircleOutline" class="success-icon"></ion-icon>
            <h3 class="result-title">이메일을 찾았습니다!</h3>
            <p class="found-email">{{ foundEmail }}</p>
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
          <span class="link-item" @click="goToFindPassword">비밀번호 찾기</span>
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

const foundEmail = ref('');

const formData = reactive({
  phoneNumber: '',
  nickname: '',
});

const errors = reactive({
  phoneNumber: '',
  nickname: '',
});

const isSubmitting = ref(false);

const validateField = (fieldName) => {
  errors[fieldName] = '';
  switch (fieldName) {
    case 'phoneNumber':
      if (!formData.phoneNumber) {
        errors.phoneNumber = '전화번호를 입력해주세요.';
      } else if (!/^01[0-9]-\d{4}-\d{4}$/.test(formData.phoneNumber)) {
        errors.phoneNumber = '올바른 전화번호 형식을 입력해주세요. (예: 010-1234-5678)';
      }
      break;
    case 'nickname':
      if (!formData.nickname) {
        errors.nickname = '닉네임을 입력해주세요.';
      } else if (formData.nickname.length < 2) {
        errors.nickname = '닉네임은 2자 이상 입력해주세요.';
      }
      break;
  }
};

const isFormValid = computed(() => {
  return formData.phoneNumber && 
         formData.nickname && 
         !errors.phoneNumber && 
         !errors.nickname;
});

const handleFindEmail = async () => {
  // 유효성 검사
  validateField('phoneNumber');
  validateField('nickname');
  if (errors.phoneNumber || errors.nickname) return;

  isSubmitting.value = true;
  foundEmail.value = '';

  try {
    const payload = {
      nickname: formData.nickname,
      phoneNumber: formData.phoneNumber,
    };

    const response = await apiClient.post('/auth/find-email', payload);

    if (response.status === 200 && response.data.maskedEmail) {
      foundEmail.value = response.data.maskedEmail;
      
      const toast = await toastController.create({
        message: '이메일을 찾았습니다!',
        duration: 2000,
        color: 'success',
        position: 'top',
      });
      await toast.present();
    } else {
      throw new Error('이메일을 찾을 수 없습니다.');
    }
  } catch (error) {
    console.error('이메일 찾기 실패:', error);
    
    let errorMessage = '입력하신 정보로 등록된 이메일을 찾을 수 없습니다.';
    if (error.response && error.response.data) {
      if (typeof error.response.data === 'string') {
        errorMessage = error.response.data;
      } else if (error.response.data.message) {
        errorMessage = error.response.data.message;
      }
    }

    const alert = await alertController.create({
      header: '이메일 찾기 실패',
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

const goToSignUp = () => {
  router.push('/auth/signup');
};
</script>

<style scoped>
/* 전체 컨테이너 */
.find-email-container {
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

/* 결과 섹션 */
.result-section {
  margin-top: 32px;
}

.result-card {
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

.result-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.found-email {
  font-size: 16px;
  color: #ff1744;
  font-weight: 600;
  margin: 0 0 24px 0;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
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