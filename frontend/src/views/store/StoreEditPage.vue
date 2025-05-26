<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title>음식점 정보 수정</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="store-edit-content">
      <div class="responsive-container" v-if="!isLoading">
        <!-- 헤더 섹션 -->
        <div class="edit-header">
          <h1 class="responsive-title">{{ formData.name }} 정보 수정</h1>
          <p class="subtitle">음식점 정보를 최신 상태로 유지하세요</p>
        </div>

        <!-- 수정 폼 -->
        <form @submit.prevent="submitForm" class="edit-form">
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

            <!-- 주소 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.address"
                placeholder="주소 검색 버튼을 클릭하세요"
                required
                readonly
                :class="{ 'ion-invalid': errors.address }"
                @ionBlur="validateField('address')"
              >
                <div slot="label">주소 <span class="required">*</span></div>
              </ion-input>
              <ion-button 
                slot="end" 
                fill="outline" 
                size="small"
                @click="searchAddress"
              >
                주소 변경
              </ion-button>
            </ion-item>
            <div v-if="errors.address" class="error-message">{{ errors.address }}</div>

            <!-- 전화번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.phoneNumber"
                placeholder="02-1234-5678"
                maxlength="14"
                :class="{ 'ion-invalid': errors.phoneNumber }"
                @ionInput="formatPhoneNumber"
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
                maxlength="12"
                :class="{ 'ion-invalid': errors.businessNumber }"
                @ionInput="formatBusinessNumber"
                @ionBlur="validateField('businessNumber')"
              >
                <div slot="label">사업자등록번호 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.businessNumber" class="error-message">{{ errors.businessNumber }}</div>
          </div>

          <!-- 운영시간 섹션 -->
          <div class="form-section">
            <h2 class="section-title">운영시간</h2>
            
            <div class="operating-hours-container">
              <div 
                v-for="day in daysOfWeek" 
                :key="day.value"
                class="day-container"
              >
                <div class="day-header-section">
                  <ion-item class="day-toggle-item" lines="none">
                    <ion-checkbox
                      slot="start"
                      v-model="formData.operatingHours[day.value].isOpen"
                      @ionChange="onDayToggle(day.value)"
                    ></ion-checkbox>
                    <ion-label class="day-label">{{ day.label }}</ion-label>
                  </ion-item>
                </div>
                
                <div 
                  v-if="formData.operatingHours[day.value].isOpen"
                  class="time-section"
                >
                  <div class="time-row">
                    <div class="time-input-group">
                      <label class="time-label">오픈</label>
                      <ion-datetime
                        v-model="formData.operatingHours[day.value].openTime"
                        presentation="time"
                        size="small"
                        class="time-picker"
                      ></ion-datetime>
                    </div>
                    <span class="time-separator">~</span>
                    <div class="time-input-group">
                      <label class="time-label">마감</label>
                      <ion-datetime
                        v-model="formData.operatingHours[day.value].closeTime"
                        presentation="time"
                        size="small"
                        class="time-picker"
                      ></ion-datetime>
                    </div>
                  </div>
                </div>
                
                <div 
                  v-else
                  class="closed-indicator"
                >
                  <span class="closed-text">휴무일</span>
                </div>
              </div>
            </div>
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

            <!-- 배달지역 설정 -->
            <div class="delivery-areas-section">
              <div class="section-header">
                <h3>배달 가능 지역</h3>
                <ion-button 
                  fill="outline" 
                  size="small"
                  @click="addDeliveryArea"
                >
                  <ion-icon :icon="addOutline" slot="start"></ion-icon>
                  지역 추가
                </ion-button>
              </div>
              
              <div v-if="formData.deliveryAreas.length === 0" class="no-areas-message">
                배달 가능 지역을 추가해주세요.
              </div>
              
              <div 
                v-for="(area, index) in formData.deliveryAreas" 
                :key="index"
                class="delivery-area-item"
              >
                <div class="area-header">
                  <span class="area-number">{{ index + 1 }}</span>
                  <ion-button 
                    fill="clear" 
                    size="small"
                    color="danger"
                    @click="removeDeliveryArea(index)"
                  >
                    <ion-icon :icon="trashOutline"></ion-icon>
                  </ion-button>
                </div>
                
                <div class="area-fields">
                  <ion-item class="area-field">
                    <ion-input
                      v-model="area.areaName"
                      placeholder="예: 강남구, 서초구"
                      required
                    >
                      <div slot="label">지역명 <span class="required">*</span></div>
                    </ion-input>
                  </ion-item>
                  
                  <ion-item class="area-field">
                    <ion-input
                      v-model="area.deliveryFee"
                      type="number"
                      placeholder="0"
                      min="0"
                      step="500"
                    >
                      <div slot="label">배달비 (원)</div>
                    </ion-input>
                  </ion-item>
                  
                  <ion-item class="area-field">
                    <ion-input
                      v-model="area.minimumOrderAmount"
                      type="number"
                      placeholder="0"
                      min="0"
                      step="1000"
                    >
                      <div slot="label">최소주문금액 (원)</div>
                    </ion-input>
                  </ion-item>
                  
                  <ion-item class="area-field">
                    <ion-input
                      v-model="area.estimatedDeliveryTime"
                      type="number"
                      placeholder="30"
                      min="10"
                      max="120"
                    >
                      <div slot="label">예상 배달시간 (분)</div>
                    </ion-input>
                  </ion-item>
                  
                  <ion-item class="area-field">
                    <ion-checkbox
                      v-model="area.isActive"
                      slot="start"
                    ></ion-checkbox>
                    <ion-label>활성화</ion-label>
                  </ion-item>
                </div>
              </div>
            </div>
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
              :disabled="isSubmitting || !isFormValid"
            >
              <ion-spinner v-if="isSubmitting" name="crescent"></ion-spinner>
              <span v-else>정보 수정하기</span>
            </ion-button>
          </div>
        </form>
      </div>

      <!-- 로딩 상태 -->
      <div v-else class="loading-container">
        <ion-spinner name="crescent"></ion-spinner>
        <p>음식점 정보를 불러오는 중...</p>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton,
  IonItem, IonInput, IonTextarea, IonSelect, IonSelectOption, IonButton,
  IonCheckbox, IonDatetime, IonSpinner, IonLabel, IonIcon, alertController, toastController
} from '@ionic/vue';
// @ts-ignore
import apiClient from '@/services/api';
import { addOutline, trashOutline } from 'ionicons/icons';

const route = useRoute();
const router = useRouter();

// 상태 관리
const isLoading = ref(true);
const isSubmitting = ref(false);
const categories = ref([]);
const storeId = route.params.id;

// 요일 데이터
const daysOfWeek = [
  { value: 'mon', label: '월요일' },
  { value: 'tue', label: '화요일' },
  { value: 'wed', label: '수요일' },
  { value: 'thu', label: '목요일' },
  { value: 'fri', label: '금요일' },
  { value: 'sat', label: '토요일' },
  { value: 'sun', label: '일요일' }
];

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
  categoryId: null,
  operatingHours: {
    'mon': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'tue': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'wed': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'thu': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'fri': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'sat': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'sun': { isOpen: true, openTime: '09:00', closeTime: '21:00' }
  },
  deliveryAreas: []
});

// 에러 상태
const errors = reactive({
  name: '',
  categoryId: '',
  address: '',
  phoneNumber: '',
  businessNumber: '',
  minimumOrderAmount: '',
  deliveryFee: ''
});

// 폼 유효성 검증
const isFormValid = computed(() => {
  return formData.name && 
         formData.categoryId && 
         formData.address && 
         formData.businessNumber &&
         formData.minimumOrderAmount >= 0 &&
         formData.deliveryFee >= 0 &&
         !Object.values(errors).some(error => error);
});

// 컴포넌트 마운트 시 데이터 로드
onMounted(async () => {
  await Promise.all([
    loadCategories(),
    loadStoreData()
  ]);
  isLoading.value = false;
});

// 카테고리 목록 로드
const loadCategories = async () => {
  try {
    const response = await apiClient.get('/categories');
    categories.value = response.data;
  } catch (error) {
    console.error('카테고리 로드 실패:', error);
    showToast('카테고리 정보를 불러오는데 실패했습니다.', 'warning');
  }
};

// 음식점 데이터 로드
const loadStoreData = async () => {
  try {
    const response = await apiClient.get(`/stores/${storeId}`);
    const store = response.data;
    
    // 폼 데이터에 기존 정보 설정
    Object.assign(formData, {
      name: store.name || '',
      description: store.description || '',
      address: store.address || '',
      phoneNumber: store.phoneNumber || '',
      businessNumber: store.businessNumber || '',
      logoImageUrl: store.logoImageUrl || '',
      minimumOrderAmount: store.minimumOrderAmount || 0,
      deliveryFee: store.deliveryFee || 0,
      categoryId: store.category?.id || store.categoryId || null
    });

    // 운영시간 정보 설정
    if (store.operatingHours && store.operatingHours.length > 0) {
      store.operatingHours.forEach(hours => {
        const day = hours.dayOfWeek.toLowerCase();
        if (formData.operatingHours[day]) {
          formData.operatingHours[day] = {
            isOpen: hours.isOpen,
            openTime: hours.openTime || '09:00',
            closeTime: hours.closeTime || '21:00'
          };
        }
      });
    }

    // 배달지역 정보 설정
    if (store.deliveryAreas && store.deliveryAreas.length > 0) {
      formData.deliveryAreas = store.deliveryAreas.map(area => ({
        ...area,
        isActive: area.isActive || true
      }));
    }
  } catch (error) {
    console.error('음식점 정보 로드 실패:', error);
    showToast('음식점 정보를 불러오는데 실패했습니다.', 'danger');
    router.push('/owner/dashboard');
  }
};

// 유효성 검증 함수들
const validateField = (field) => {
  switch (field) {
    case 'name':
      errors.name = !formData.name ? '가게명을 입력해주세요.' : '';
      break;
    case 'categoryId':
      errors.categoryId = !formData.categoryId ? '카테고리를 선택해주세요.' : '';
      break;
    case 'address':
      errors.address = !formData.address ? '주소를 입력해주세요.' : '';
      break;
    case 'phoneNumber':
      if (formData.phoneNumber) {
        const phoneRegex = /^0\d{1,2}-\d{3,4}-\d{4}$/;
        errors.phoneNumber = !phoneRegex.test(formData.phoneNumber) ? '올바른 전화번호 형식이 아닙니다.' : '';
      } else {
        errors.phoneNumber = '';
      }
      break;
    case 'businessNumber':
      const businessRegex = /^\d{3}-\d{2}-\d{5}$/;
      errors.businessNumber = !businessRegex.test(formData.businessNumber) ? '올바른 사업자등록번호 형식이 아닙니다.' : '';
      break;
    case 'minimumOrderAmount':
      errors.minimumOrderAmount = formData.minimumOrderAmount < 0 ? '최소주문금액은 0원 이상이어야 합니다.' : '';
      break;
    case 'deliveryFee':
      errors.deliveryFee = formData.deliveryFee < 0 ? '배달비는 0원 이상이어야 합니다.' : '';
      break;
  }
};

// 토스트 메시지 표시
const showToast = async (message, color = 'primary') => {
  const toast = await toastController.create({
    message,
    duration: 3000,
    color,
    position: 'bottom'
  });
  await toast.present();
};

// 사업자등록번호 자동 포맷팅
const formatBusinessNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, '');
  
  if (value.length <= 3) {
    formData.businessNumber = value;
  } else if (value.length <= 5) {
    formData.businessNumber = value.slice(0, 3) + '-' + value.slice(3);
  } else if (value.length <= 10) {
    formData.businessNumber = value.slice(0, 3) + '-' + value.slice(3, 5) + '-' + value.slice(5);
  } else {
    formData.businessNumber = value.slice(0, 3) + '-' + value.slice(3, 5) + '-' + value.slice(5, 10);
  }
};

// 전화번호 자동 포맷팅
const formatPhoneNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, ''); // 숫자만 남기기
  
  if (value.length === 0) {
    formData.phoneNumber = '';
    return;
  }
  
  // 010으로 시작하는 휴대폰 번호
  if (value.startsWith('010')) {
    if (value.length <= 3) {
      formData.phoneNumber = value;
    } else if (value.length <= 7) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3);
    } else if (value.length <= 11) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7);
    } else {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
    }
  }
  // 02로 시작하는 서울 지역번호 (8자리)
  else if (value.startsWith('02')) {
    if (value.length <= 2) {
      formData.phoneNumber = value;
    } else if (value.length <= 5) {
      formData.phoneNumber = value.slice(0, 2) + '-' + value.slice(2);
    } else if (value.length <= 9) {
      formData.phoneNumber = value.slice(0, 2) + '-' + value.slice(2, 5) + '-' + value.slice(5);
    } else {
      formData.phoneNumber = value.slice(0, 2) + '-' + value.slice(2, 5) + '-' + value.slice(5, 9);
    }
  }
  // 기타 지역번호 (031, 032, 033, 041, 042, 043, 044, 051, 052, 053, 054, 055, 061, 062, 063, 064)
  else if (/^(031|032|033|041|042|043|044|051|052|053|054|055|061|062|063|064)/.test(value)) {
    if (value.length <= 3) {
      formData.phoneNumber = value;
    } else if (value.length <= 6) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3);
    } else if (value.length <= 10) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 6) + '-' + value.slice(6);
    } else {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 6) + '-' + value.slice(6, 10);
    }
  }
  // 기타 번호는 기본 형식으로
  else {
    if (value.length <= 3) {
      formData.phoneNumber = value;
    } else if (value.length <= 7) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3);
    } else if (value.length <= 11) {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7);
    } else {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
    }
  }
};

// 주소 검색 함수
const searchAddress = () => {
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: function(data) {
        formData.address = data.address;
        validateField('address');
      }
    }).open();
  } else {
    showToast('주소 검색 서비스를 불러올 수 없습니다.', 'warning');
  }
};

// 운영시간 토글
const onDayToggle = (day) => {
  formData.operatingHours[day].isOpen = !formData.operatingHours[day].isOpen;
};

// 배달지역 추가
const addDeliveryArea = () => {
  formData.deliveryAreas.push({
    areaName: '',
    deliveryFee: 0,
    minimumOrderAmount: 0,
    estimatedDeliveryTime: 30,
    isActive: true
  });
};

// 배달지역 제거
const removeDeliveryArea = (index) => {
  formData.deliveryAreas.splice(index, 1);
};

// 폼 제출
const submitForm = async () => {
  // 모든 필드 유효성 검증
  Object.keys(errors).forEach(field => validateField(field));

  if (!isFormValid.value) {
    showToast('입력 정보를 확인해주세요.', 'warning');
    return;
  }

  isSubmitting.value = true;

  try {
    const submitData = {
      ...formData,
      operatingHours: Object.entries(formData.operatingHours).map(([day, hours]) => ({
        dayOfWeek: day.toUpperCase(),
        isOpen: hours.isOpen,
        openTime: hours.isOpen ? hours.openTime : null,
        closeTime: hours.isOpen ? hours.closeTime : null
      })),
      deliveryAreas: formData.deliveryAreas.map(area => ({
        ...area,
        isActive: area.isActive
      }))
    };
    
    await apiClient.put(`/stores/${storeId}`, submitData);
    
    // 성공 알림
    const alert = await alertController.create({
      header: '수정 완료',
      message: '음식점 정보가 성공적으로 수정되었습니다!',
      buttons: [
        {
          text: '확인',
          handler: () => {
            router.push('/owner/dashboard');
          }
        }
      ]
    });
    await alert.present();
    
  } catch (error) {
    console.error('음식점 정보 수정 실패:', error);
    
    let errorMessage = '음식점 정보 수정에 실패했습니다.';
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error;
    } else if (error.response?.status === 400) {
      errorMessage = '입력 정보를 확인해주세요.';
    } else if (error.response?.status === 403) {
      errorMessage = '수정 권한이 없습니다.';
    } else if (error.response?.status === 409) {
      errorMessage = '이미 등록된 사업자등록번호입니다.';
    } else if (error.response?.status === 500) {
      errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
    
    showToast(errorMessage, 'danger');
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.store-edit-content {
  --background: var(--ion-color-light);
}

.responsive-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.edit-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, var(--ion-color-primary), var(--ion-color-secondary));
  border-radius: 16px;
  color: white;
}

.responsive-title {
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0 0 10px 0;
}

.subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
}

.edit-form {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: var(--ion-color-dark);
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--ion-color-light);
  text-align: center;
}

.form-item {
  margin-bottom: 16px;
  --border-radius: 12px;
  --background: var(--ion-color-light-tint);
  --border-color: var(--ion-color-medium-tint);
}

.form-item.ion-invalid {
  --border-color: var(--ion-color-danger);
}

.required {
  color: var(--ion-color-danger);
  font-weight: 600;
}

.error-message {
  color: var(--ion-color-danger);
  font-size: 0.875rem;
  margin-top: 4px;
  margin-left: 16px;
}

.form-note {
  font-size: 0.875rem;
  color: var(--ion-color-medium);
  margin-top: 8px;
  margin-left: 16px;
}

/* 운영시간 스타일 */
.operating-hours-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.day-container {
  background: var(--ion-color-light-tint);
  border-radius: 12px;
  border: 1px solid var(--ion-color-medium-tint);
  padding: 16px;
  transition: all 0.3s ease;
}

.day-container:hover {
  border-color: var(--ion-color-primary-tint);
  box-shadow: 0 2px 8px rgba(var(--ion-color-primary-rgb), 0.1);
}

.day-header-section {
  margin-bottom: 12px;
}

.day-toggle-item {
  --background: transparent;
  --padding-start: 0;
  --padding-end: 0;
  --inner-padding-end: 0;
  --min-height: auto;
}

.day-label {
  font-weight: 600;
  color: var(--ion-color-dark);
  font-size: 1.1rem;
  margin-left: 12px;
}

.time-section {
  padding-left: 40px;
}

.time-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.time-input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.time-label {
  font-weight: 500;
  color: var(--ion-color-medium);
  font-size: 0.9rem;
  text-align: center;
}

.time-separator {
  color: var(--ion-color-primary);
  font-weight: 600;
  font-size: 1.2rem;
  margin: 0 8px;
  align-self: flex-end;
  margin-bottom: 8px;
}

.time-picker {
  --background: white;
  --border-radius: 8px;
  --border-width: 1px;
  --border-color: var(--ion-color-medium-tint);
  --padding-start: 12px;
  --padding-end: 12px;
  --color: var(--ion-color-dark);
  min-width: 120px;
  text-align: center;
  font-weight: 500;
}

.time-picker:hover {
  --border-color: var(--ion-color-primary);
}

.closed-indicator {
  text-align: center;
  padding: 20px;
  background: var(--ion-color-light);
  border-radius: 8px;
  margin-left: 40px;
}

.closed-text {
  font-weight: 500;
  color: var(--ion-color-medium);
  font-size: 1rem;
  font-style: italic;
}

.submit-section {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--ion-color-light);
}

.submit-button {
  --background: linear-gradient(135deg, var(--ion-color-primary), var(--ion-color-secondary));
  --background-activated: linear-gradient(135deg, var(--ion-color-primary-shade), var(--ion-color-secondary-shade));
  --background-hover: linear-gradient(135deg, var(--ion-color-primary-tint), var(--ion-color-secondary-tint));
  --border-radius: 12px;
  --box-shadow: 0 4px 16px rgba(var(--ion-color-primary-rgb), 0.3);
  --color: white;
  --padding-top: 16px;
  --padding-bottom: 16px;
  font-weight: 600;
  font-size: 1.1rem;
  margin: 0;
}

.submit-button:hover {
  --box-shadow: 0 6px 20px rgba(var(--ion-color-primary-rgb), 0.4);
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

.submit-button[disabled] {
  --background: var(--ion-color-medium);
  --color: var(--ion-color-medium-contrast);
  --box-shadow: none;
  transform: none;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
  gap: 16px;
}

.loading-container p {
  color: var(--ion-color-medium);
  font-size: 1rem;
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .responsive-container {
    padding: 16px;
  }
  
  .responsive-title {
    font-size: 1.5rem;
  }
  
  .edit-form {
    padding: 20px;
  }
  
  .day-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .time-section {
    width: 100%;
    justify-content: space-between;
  }
  
  .time-inputs {
    max-width: 140px;
  }
}

@media (min-width: 768px) {
  .responsive-container {
    padding: 32px;
  }
  
  .edit-form {
    padding: 32px;
  }
}

/* 배달지역 스타일 */
.delivery-areas-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--ion-color-light);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ion-color-dark);
  margin: 0;
}

.no-areas-message {
  text-align: center;
  color: var(--ion-color-medium);
  font-style: italic;
  padding: 20px;
  background: var(--ion-color-light-tint);
  border-radius: 8px;
  margin-bottom: 16px;
}

.delivery-area-item {
  background: var(--ion-color-light-tint);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid var(--ion-color-medium-tint);
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.area-number {
  background: var(--ion-color-primary);
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: 600;
}

.area-fields {
  display: grid;
  gap: 12px;
}

.area-field {
  --background: white;
  --border-radius: 8px;
  --border-color: var(--ion-color-medium-tint);
  margin-bottom: 0;
}

@media (min-width: 768px) {
  .area-fields {
    grid-template-columns: 1fr 1fr;
  }
  
  .area-field:last-child {
    grid-column: 1 / -1;
  }
}
</style> 