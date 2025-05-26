<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title>음식점 등록</ion-title>
        <ion-buttons slot="end" v-if="isDevelopment">
          <ion-button fill="clear" @click="fillDummyData">
            <ion-icon :icon="flashOutline"></ion-icon>
            더미 데이터
          </ion-button>
        </ion-buttons>
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
                주소 검색
              </ion-button>
            </ion-item>
            <div v-if="errors.address" class="error-message">{{ errors.address }}</div>

            <!-- 상세주소 -->
            <ion-item class="form-item" v-if="formData.address">
              <ion-input
                v-model="formData.detailAddress"
                placeholder="상세주소를 입력하세요 (동, 호수 등)"
              >
                <div slot="label">상세주소</div>
              </ion-input>
            </ion-item>

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
            
            <!-- 로고 이미지 업로드 -->
            <div class="image-upload-section">
              <h3 class="field-label">가게 로고</h3>
              <ImageUploader
                image-type="logo"
                :auto-upload="false"
                :existing-image-url="formData.logoImageUrl"
                @image-selected="onLogoImageSelected"
                @image-removed="onLogoImageRemoved"
              />
              <div class="form-note">로고 이미지는 나중에 추가할 수 있습니다.</div>
            </div>
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
  IonCheckbox,
  IonDatetime,
  IonLabel,
  IonIcon,
  toastController,
  alertController
} from '@ionic/vue';
import { categoryApi, storeApi } from '@/services/storeApi.js';
import { fileUploadApi } from '@/services/api.js';
import { addOutline, trashOutline, flashOutline } from 'ionicons/icons';
import ImageUploader from '@/components/ImageUploader.vue';

const router = useRouter();

// 상태 관리
const isLoading = ref(false);
const categories = ref([]);
const isDevelopment = ref(import.meta.env.DEV);
const selectedLogoFile = ref(null);

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
  detailAddress: '',
  phoneNumber: '',
  businessNumber: '',
  logoImageUrl: '',
  minimumOrderAmount: 0,
  deliveryFee: 0,
  categoryId: null,
  deliveryAreas: [],
  operatingHours: {
    'mon': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'tue': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'wed': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'thu': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'fri': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'sat': { isOpen: true, openTime: '09:00', closeTime: '21:00' },
    'sun': { isOpen: true, openTime: '09:00', closeTime: '21:00' }
  }
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

// 이미지 업로드 관련 이벤트 핸들러
const onLogoImageSelected = (file) => {
  console.log('로고 이미지 선택됨:', file);
  selectedLogoFile.value = file;
};

const onLogoImageRemoved = () => {
  console.log('로고 이미지 제거됨');
  selectedLogoFile.value = null;
  formData.logoImageUrl = '';
};

// 이미지 업로드 함수
const uploadLogoImage = async (storeId) => {
  if (!selectedLogoFile.value) {
    return null;
  }

  try {
    const formDataForUpload = new FormData();
    formDataForUpload.append('image', selectedLogoFile.value);
    
    const response = await fileUploadApi.uploadStoreLogo(storeId, formDataForUpload);
    console.log('로고 이미지 업로드 성공:', response.data);
    return response.data.imageUrl;
  } catch (error) {
    console.error('로고 이미지 업로드 실패:', error);
    throw error;
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
    // 1단계: 가게 정보 먼저 등록
    const submitData = {
      ...formData,
      address: formData.detailAddress 
        ? `${formData.address} ${formData.detailAddress}` 
        : formData.address,
      // 운영시간 데이터 포맷팅
      operatingHours: Object.entries(formData.operatingHours).map(([day, hours]) => ({
        dayOfWeek: day.toUpperCase(),
        isOpen: hours.isOpen,
        openTime: hours.isOpen ? hours.openTime : null,
        closeTime: hours.isOpen ? hours.closeTime : null
      }))
    };
    
    const storeResponse = await storeApi.createStore(submitData);
    const storeId = storeResponse.data.id;
    
    // 2단계: 로고 이미지가 선택되었다면 업로드
    if (selectedLogoFile.value) {
      try {
        const imageUrl = await uploadLogoImage(storeId);
        console.log('로고 이미지 업로드 완료:', imageUrl);
        showToast('가게 등록 및 로고 업로드가 완료되었습니다!', 'success');
      } catch (imageError) {
        console.error('로고 이미지 업로드 실패:', imageError);
        showToast('가게는 등록되었지만 로고 업로드에 실패했습니다. 나중에 다시 시도해주세요.', 'warning');
      }
    } else {
      showToast('가게가 성공적으로 등록되었습니다!', 'success');
    }
    
    // 성공 알림
    const alert = await alertController.create({
      header: '등록 완료',
      message: '음식점이 성공적으로 등록되었습니다!',
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
    console.error('음식점 등록 실패:', error);
    
    let errorMessage = '음식점 등록에 실패했습니다.';
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error;
    } else if (error.response?.status === 400) {
      errorMessage = '입력 정보를 확인해주세요.';
    } else if (error.response?.status === 409) {
      errorMessage = '이미 등록된 사업자등록번호입니다.';
    } else if (error.response?.status === 500) {
      errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
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

// 사업자등록번호 입력 시 자동으로 하이픈을 추가하는 함수
const formatBusinessNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, ''); // 숫자만 남기기
  
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

// 주소 검색 함수 (카카오 주소 API)
const searchAddress = () => {
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: function(data) {
        // 선택된 주소 정보를 formData에 저장
        formData.address = data.address;
        // 상세주소 입력 필드에 포커스
        setTimeout(() => {
          const detailAddressInput = document.querySelector('ion-input[placeholder*="상세주소"]');
          if (detailAddressInput) {
            detailAddressInput.setFocus();
          }
        }, 100);
      }
    }).open();
  } else {
    showToast('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.', 'warning');
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

// 운영시간 토글
const onDayToggle = (day) => {
  formData.operatingHours[day].isOpen = !formData.operatingHours[day].isOpen;
};

// 배달지역 추가
const addDeliveryArea = () => {
  formData.deliveryAreas.push({
    areaName: '',
    deliveryFee: formData.deliveryFee || 0,
    minimumOrderAmount: formData.minimumOrderAmount || 0,
    estimatedDeliveryTime: 30,
    isActive: true
  });
};

// 배달지역 제거
const removeDeliveryArea = (index) => {
  formData.deliveryAreas.splice(index, 1);
};

// 더미 데이터 채우기
const fillDummyData = () => {
  // 기본 정보
  formData.name = '맛있는 치킨집';
  formData.description = '바삭하고 맛있는 치킨을 제공하는 전문점입니다. 신선한 재료와 특제 소스로 만든 치킨을 맛보세요!';
  formData.address = '서울특별시 강남구 테헤란로 123';
  formData.detailAddress = '456호';
  formData.phoneNumber = '02-1234-5678';
  formData.businessNumber = '123-45-67890';
  // 로고 이미지는 실제 파일 업로드로 처리
  
  // 배달 정보
  formData.minimumOrderAmount = 15000;
  formData.deliveryFee = 3000;
  
  // 카테고리 (치킨 카테고리가 있다면 자동 선택)
  const chickenCategory = categories.value.find(cat => cat.name.includes('치킨'));
  if (chickenCategory) {
    formData.categoryId = chickenCategory.id;
  } else if (categories.value.length > 0) {
    formData.categoryId = categories.value[0].id;
  }
  
  // 운영시간 (평일 11:00-22:00, 주말 12:00-23:00)
  formData.operatingHours = {
    'mon': { isOpen: true, openTime: '11:00', closeTime: '22:00' },
    'tue': { isOpen: true, openTime: '11:00', closeTime: '22:00' },
    'wed': { isOpen: true, openTime: '11:00', closeTime: '22:00' },
    'thu': { isOpen: true, openTime: '11:00', closeTime: '22:00' },
    'fri': { isOpen: true, openTime: '11:00', closeTime: '22:00' },
    'sat': { isOpen: true, openTime: '12:00', closeTime: '23:00' },
    'sun': { isOpen: true, openTime: '12:00', closeTime: '23:00' }
  };
  
  // 배달지역 더미 데이터
  formData.deliveryAreas = [
    {
      areaName: '강남구',
      deliveryFee: 3000,
      minimumOrderAmount: 15000,
      estimatedDeliveryTime: 30,
      isActive: true
    },
    {
      areaName: '서초구',
      deliveryFee: 3500,
      minimumOrderAmount: 18000,
      estimatedDeliveryTime: 35,
      isActive: true
    },
    {
      areaName: '송파구',
      deliveryFee: 4000,
      minimumOrderAmount: 20000,
      estimatedDeliveryTime: 40,
      isActive: true
    }
  ];
  
  showToast('더미 데이터가 입력되었습니다! 🚀', 'success');
};
</script>

<style scoped>
.store-registration-content {
  --background: var(--ion-color-light);
}

.responsive-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.registration-header {
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

.registration-form {
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

/* 이미지 업로드 섹션 스타일 */
.image-upload-section {
  padding: 20px 0;
}

.field-label {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--ion-color-dark);
  margin-bottom: 12px;
  text-align: center;
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .responsive-container {
    padding: 16px;
  }
  
  .responsive-title {
    font-size: 1.5rem;
  }
  
  .registration-form {
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
  
  .time-picker {
    max-width: 140px;
  }
}

@media (min-width: 768px) {
  .responsive-container {
    padding: 32px;
  }
  
  .registration-form {
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