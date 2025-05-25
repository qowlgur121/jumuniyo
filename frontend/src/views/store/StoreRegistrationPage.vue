<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>음식점 등록</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="store-registration-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="registration-header">
          <h1 class="responsive-title">새로운 음식점을 등록하세요</h1>
          <p class="subtitle">주문이요와 함께 더 많은 고객을 만나보세요!</p>
        </div>

        <!-- 등록 폼 -->
        <form @submit.prevent="submitForm" class="registration-form">
          <!-- 기본 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">기본 정보</h2>
            
            <!-- 가게명 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.name"
                placeholder="가게명을 입력하세요"
                required
                :class="{ 'ion-invalid': errors.name }"
                @ionBlur="validateField('name')"
              >
                <div slot="label">가게명 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.name" class="error-message">{{ errors.name }}</div>

            <!-- 가게 설명 -->
            <ion-item class="form-item">
              <ion-textarea
                v-model="formData.description"
                placeholder="가게에 대한 간단한 소개를 입력하세요"
                rows="3"
                auto-grow
              >
                <div slot="label">가게 설명</div>
              </ion-textarea>
            </ion-item>

            <!-- 카테고리 -->
            <ion-item class="form-item">
              <ion-select
                v-model="formData.categoryId"
                placeholder="카테고리를 선택하세요"
                interface="action-sheet"
                required
                :class="{ 'ion-invalid': errors.categoryId }"
                @ionChange="validateField('categoryId')"
              >
                <div slot="label">카테고리 <span class="required">*</span></div>
                <ion-select-option
                  v-for="category in categories"
                  :key="category.id"
                  :value="category.id"
                >
                  {{ category.name }}
                </ion-select-option>
              </ion-select>
            </ion-item>
            <div v-if="errors.categoryId" class="error-message">{{ errors.categoryId }}</div>
          </div>

          <!-- 연락처 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">연락처 정보</h2>
            
            <!-- 주소 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.address"
                placeholder="가게 주소를 입력하세요"
                required
                :class="{ 'ion-invalid': errors.address }"
                @ionBlur="validateField('address')"
              >
                <div slot="label">주소 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.address" class="error-message">{{ errors.address }}</div>

            <!-- 전화번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.phoneNumber"
                placeholder="02-1234-5678"
                :class="{ 'ion-invalid': errors.phoneNumber }"
                @ionBlur="validateField('phoneNumber')"
              >
                <div slot="label">전화번호</div>
              </ion-input>
            </ion-item>
            <div v-if="errors.phoneNumber" class="error-message">{{ errors.phoneNumber }}</div>
          </div>

          <!-- 사업자 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">사업자 정보</h2>
            
            <!-- 사업자등록번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.businessNumber"
                placeholder="123-45-67890"
                required
                :class="{ 'ion-invalid': errors.businessNumber }"
                @ionBlur="validateField('businessNumber')"
              >
                <div slot="label">사업자등록번호 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.businessNumber" class="error-message">{{ errors.businessNumber }}</div>
          </div>

          <!-- 배달 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">배달 정보</h2>
            
            <!-- 최소주문금액 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.minimumOrderAmount"
                type="number"
                placeholder="0"
                min="0"
                step="1000"
                required
                :class="{ 'ion-invalid': errors.minimumOrderAmount }"
                @ionBlur="validateField('minimumOrderAmount')"
              >
                <div slot="label">최소주문금액 (원) <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.minimumOrderAmount" class="error-message">{{ errors.minimumOrderAmount }}</div>

            <!-- 배달비 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.deliveryFee"
                type="number"
                placeholder="0"
                min="0"
                step="500"
                required
                :class="{ 'ion-invalid': errors.deliveryFee }"
                @ionBlur="validateField('deliveryFee')"
              >
                <div slot="label">배달비 (원) <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.deliveryFee" class="error-message">{{ errors.deliveryFee }}</div>
          </div>

          <!-- 추가 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">추가 정보</h2>
            
            <!-- 로고 이미지 URL -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.logoImageUrl"
                placeholder="https://example.com/logo.jpg"
                type="url"
              >
                <div slot="label">로고 이미지 URL</div>
              </ion-input>
            </ion-item>
            <div class="form-note">로고 이미지는 나중에 추가할 수 있습니다.</div>
          </div>

          <!-- 제출 버튼 -->
          <div class="submit-section">
            <ion-button
              type="submit"
              expand="block"
              size="large"
              class="submit-button"
              :disabled="isLoading || !isFormValid"
            >
              <ion-spinner v-if="isLoading" name="crescent"></ion-spinner>
              <span v-else>음식점 등록하기</span>
            </ion-button>
          </div>
        </form>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
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
  IonTextarea,
  IonSelect,
  IonSelectOption,
  IonButton,
  IonSpinner,
  toastController,
  alertController
} from '@ionic/vue';
import { categoryApi, storeApi } from '@/services/storeApi.js';

const router = useRouter();

// 상태 관리
const isLoading = ref(false);
const categories = ref([]);

// 폼 데이터
const formData = reactive({
  name: '',
  description: '',
  address: '',
  phoneNumber: '',
  businessNumber: '',
  logoImageUrl: '',
  minimumOrderAmount: 0,
  deliveryFee: 0,
  categoryId: null
});

// 에러 상태
const errors = reactive({
  name: '',
  address: '',
  phoneNumber: '',
  businessNumber: '',
  minimumOrderAmount: '',
  deliveryFee: '',
  categoryId: ''
});

// 폼 유효성 체크
const isFormValid = computed(() => {
  return formData.name && 
         formData.address && 
         formData.businessNumber && 
         formData.categoryId &&
         !Object.values(errors).some(error => error);
});

// 마운트 시 카테고리 목록 로드
onMounted(async () => {
  await loadCategories();
});

// 카테고리 목록 로드
const loadCategories = async () => {
  try {
    const response = await categoryApi.getActiveCategories();
    categories.value = response.data;
  } catch (error) {
    console.error('카테고리 로드 실패:', error);
    showToast('카테고리 정보를 불러오는데 실패했습니다.', 'warning');
  }
};

// 필드별 유효성 검증
const validateField = (fieldName) => {
  errors[fieldName] = '';

  switch (fieldName) {
    case 'name':
      if (!formData.name) {
        errors.name = '가게명은 필수 입력 값입니다.';
      } else if (formData.name.length > 100) {
        errors.name = '가게명은 최대 100자까지 입력 가능합니다.';
      }
      break;

    case 'address':
      if (!formData.address) {
        errors.address = '주소는 필수 입력 값입니다.';
      } else if (formData.address.length > 255) {
        errors.address = '주소는 최대 255자까지 입력 가능합니다.';
      }
      break;

    case 'phoneNumber':
      if (formData.phoneNumber && !/^\d{2,3}-\d{3,4}-\d{4}$/.test(formData.phoneNumber)) {
        errors.phoneNumber = '전화번호는 올바른 형식으로 입력해주세요. (예: 02-1234-5678)';
      }
      break;

    case 'businessNumber':
      if (!formData.businessNumber) {
        errors.businessNumber = '사업자등록번호는 필수 입력 값입니다.';
      } else if (!/^\d{3}-\d{2}-\d{5}$/.test(formData.businessNumber)) {
        errors.businessNumber = '사업자등록번호는 올바른 형식으로 입력해주세요. (예: 123-45-67890)';
      }
      break;

    case 'minimumOrderAmount':
      if (formData.minimumOrderAmount < 0) {
        errors.minimumOrderAmount = '최소주문금액은 0원 이상이어야 합니다.';
      }
      break;

    case 'deliveryFee':
      if (formData.deliveryFee < 0) {
        errors.deliveryFee = '배달비는 0원 이상이어야 합니다.';
      }
      break;

    case 'categoryId':
      if (!formData.categoryId) {
        errors.categoryId = '카테고리는 필수 선택 값입니다.';
      }
      break;
  }
};

// 폼 제출
const submitForm = async () => {
  // 모든 필드 유효성 검증
  Object.keys(errors).forEach(field => validateField(field));

  if (!isFormValid.value) {
    showToast('입력 정보를 확인해주세요.', 'warning');
    return;
  }

  isLoading.value = true;

  try {
    const response = await storeApi.createStore(formData);
    
    // 성공 알림
    const alert = await alertController.create({
      header: '등록 완료',
      message: '음식점이 성공적으로 등록되었습니다!\n승인 후 서비스를 이용하실 수 있습니다.',
      buttons: [
        {
          text: '확인',
          handler: () => {
            router.push('/store/my');
          }
        }
      ]
    });
    await alert.present();

  } catch (error) {
    console.error('음식점 등록 실패:', error);
    
    let errorMessage = '음식점 등록 중 오류가 발생했습니다.';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    showToast(errorMessage, 'danger');
  } finally {
    isLoading.value = false;
  }
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
.store-registration-content {
  --background: var(--ion-color-light);
}

.registration-header {
  text-align: center;
  padding: 2rem 0;
  background: linear-gradient(135deg, var(--yogiyo-primary) 0%, #e91e63 100%);
  color: white;
  margin-bottom: 2rem;
}

.registration-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.subtitle {
  margin: 0;
  font-size: 1rem;
  opacity: 0.9;
}

.registration-form {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 1rem 2rem;
}

.form-section {
  margin-bottom: 2rem;
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--yogiyo-primary);
  border-bottom: 2px solid var(--yogiyo-primary);
  padding-bottom: 0.5rem;
}

.form-item {
  margin-bottom: 1rem;
  --border-color: #e0e0e0;
  --highlight-color: var(--yogiyo-primary);
}

.form-item:last-child {
  margin-bottom: 0;
}

.required {
  color: var(--yogiyo-primary);
}

.error-message {
  color: var(--ion-color-danger);
  font-size: 0.875rem;
  margin: 0.25rem 0 0.5rem 1rem;
}

.form-note {
  color: var(--ion-color-medium);
  font-size: 0.875rem;
  margin-top: 0.5rem;
  font-style: italic;
}

.submit-section {
  margin-top: 2rem;
  padding: 0 1rem;
}

.submit-button {
  --background: var(--yogiyo-primary);
  --background-hover: var(--yogiyo-primary-dark);
  --color: white;
  font-weight: 600;
  height: 56px;
}

.submit-button:disabled {
  --background: var(--ion-color-medium);
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .registration-header {
    padding: 1.5rem 1rem;
  }
  
  .registration-header h1 {
    font-size: 1.3rem;
  }
  
  .subtitle {
    font-size: 0.9rem;
  }
  
  .form-section {
    margin: 0 0 1.5rem 0;
    border-radius: 8px;
    padding: 1rem;
  }
  
  .section-title {
    font-size: 1.1rem;
  }
}

@media (min-width: 768px) {
  .registration-form {
    padding: 0 2rem 2rem;
  }
}
</style> 