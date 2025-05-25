<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/auth/login"></ion-back-button>
        </ion-buttons>
        <ion-title>사장님 회원가입</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="owner-signup-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="signup-header">
          <div class="logo-section">
            <div class="logo">
              <span class="logo-text">주문이요</span>
              <span class="owner-badge">사장님</span>
            </div>
            <h1 class="responsive-title">사장님 전용 가입</h1>
            <p class="subtitle">주문이요와 함께 더 많은 고객을 만나보세요!</p>
          </div>
        </div>

        <!-- 회원가입 폼 -->
        <form @submit.prevent="handleSubmit" class="signup-form">
          <!-- 기본 정보 섹션 -->
          <div class="form-section">
            <h2 class="section-title">기본 정보</h2>
            
            <!-- 이메일 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.email"
                type="email"
                placeholder="example@email.com"
                required
                :class="{ 'ion-invalid': errors.email }"
                @ionBlur="validateField('email')"
              >
                <div slot="label">이메일 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.email" class="error-message">{{ errors.email }}</div>

            <!-- 비밀번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.password"
                type="password"
                placeholder="영문, 숫자, 특수문자 포함 8자 이상"
                required
                :class="{ 'ion-invalid': errors.password }"
                @ionBlur="validateField('password')"
              >
                <div slot="label">비밀번호 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.password" class="error-message">{{ errors.password }}</div>

            <!-- 비밀번호 확인 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="비밀번호를 다시 입력하세요"
                required
                :class="{ 'ion-invalid': errors.confirmPassword }"
                @ionBlur="validateField('confirmPassword')"
              >
                <div slot="label">비밀번호 확인 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</div>

            <!-- 닉네임 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.nickname"
                placeholder="2자 이상 20자 이하"
                required
                :class="{ 'ion-invalid': errors.nickname }"
                @ionBlur="validateField('nickname')"
              >
                <div slot="label">닉네임 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.nickname" class="error-message">{{ errors.nickname }}</div>

            <!-- 전화번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.phoneNumber"
                placeholder="전화번호를 입력하세요"
                required
                :class="{ 'ion-invalid': errors.phoneNumber }"
                @ionInput="formatPhoneNumber"
                @ionBlur="validateField('phoneNumber')"
              >
                <div slot="label">전화번호 <span class="required">*</span></div>
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
                placeholder="사업자등록번호를 입력하세요"
                required
                :class="{ 'ion-invalid': errors.businessNumber }"
                @ionInput="formatBusinessNumber"
                @ionBlur="validateField('businessNumber')"
              >
                <div slot="label">사업자등록번호 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.businessNumber" class="error-message">{{ errors.businessNumber }}</div>

            <!-- 가게명 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.storeName"
                placeholder="가게명을 입력하세요"
                required
                :class="{ 'ion-invalid': errors.storeName }"
                @ionBlur="validateField('storeName')"
              >
                <div slot="label">가게명 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.storeName" class="error-message">{{ errors.storeName }}</div>

            <!-- 가게 전화번호 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.storePhoneNumber"
                placeholder="가게 전화번호를 입력하세요"
                required
                :class="{ 'ion-invalid': errors.storePhoneNumber }"
                @ionInput="formatStorePhoneNumber"
                @ionBlur="validateField('storePhoneNumber')"
              >
                <div slot="label">가게 전화번호 <span class="required">*</span></div>
              </ion-input>
            </ion-item>
            <div v-if="errors.storePhoneNumber" class="error-message">{{ errors.storePhoneNumber }}</div>

            <!-- 가게 주소 -->
            <ion-item class="form-item address-item">
              <div slot="label">가게 주소 <span class="required">*</span></div>
              <ion-input
                v-model="formData.storeAddress"
                placeholder="주소 검색을 클릭하세요"
                readonly
                required
                :class="{ 'ion-invalid': errors.storeAddress }"
              ></ion-input>
              <ion-button 
                fill="outline" 
                size="small" 
                slot="end"
                @click="openAddressSearch"
                class="address-search-btn"
              >
                주소 검색
              </ion-button>
            </ion-item>
            <div v-if="errors.storeAddress" class="error-message">{{ errors.storeAddress }}</div>

            <!-- 주소 상세 정보 -->
            <ion-item class="form-item">
              <ion-input
                v-model="formData.storeAddressDetail"
                placeholder="상세 주소를 입력하세요 (예: 301호, 지하 1층)"
                :class="{ 'ion-invalid': errors.storeAddressDetail }"
                @ionBlur="validateField('storeAddressDetail')"
              >
                <div slot="label">상세 주소</div>
              </ion-input>
            </ion-item>
            <div v-if="errors.storeAddressDetail" class="error-message">{{ errors.storeAddressDetail }}</div>
          </div>

          <!-- 약관 동의 -->
          <div class="terms-section">
            <ion-checkbox
              v-model="formData.agreeToTerms"
              :class="{ 'ion-invalid': errors.agreeToTerms }"
            ></ion-checkbox>
            <label @click="formData.agreeToTerms = !formData.agreeToTerms" class="terms-label">
              주문이요 사장님 서비스 이용약관 및 개인정보처리방침에 동의합니다. <span class="required">*</span>
            </label>
          </div>
          <div v-if="errors.agreeToTerms" class="error-message">{{ errors.agreeToTerms }}</div>

          <!-- 가입 버튼 -->
          <ion-button
            type="submit"
            expand="block"
            size="large"
            class="signup-button"
            :disabled="isLoading"
          >
            <ion-spinner v-if="isLoading" name="crescent"></ion-spinner>
            <span v-else>사장님 계정 만들기</span>
          </ion-button>
        </form>

        <!-- 하단 링크 -->
        <div class="bottom-links">
          <p>이미 사장님 계정이 있나요? <span @click="goToLogin" class="link">로그인</span></p>
          <p>일반 고객으로 가입하시나요? <span @click="goToCustomerSignUp" class="link">고객 회원가입</span></p>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
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
  IonCheckbox,
  IonButton,
  IonSpinner,
  toastController,
  alertController
} from '@ionic/vue';
import apiClient from '@/services/api.js';

const router = useRouter();

// 상태 관리
const isLoading = ref(false);

// 폼 데이터
const formData = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phoneNumber: '',
  businessNumber: '',
  storeName: '',
  storeAddress: '',
  storeAddressDetail: '',
  storePhoneNumber: '',
  agreeToTerms: false
});

// 에러 상태
const errors = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phoneNumber: '',
  businessNumber: '',
  storeName: '',
  storeAddress: '',
  storeAddressDetail: '',
  storePhoneNumber: '',
  agreeToTerms: ''
});

// 폼 유효성 체크
const isFormValid = computed(() => {
  // 필수 필드들이 모두 채워져 있는지 확인
  const requiredFieldsFilled = formData.email && 
                              formData.password && 
                              formData.confirmPassword &&
                              formData.nickname && 
                              formData.phoneNumber &&
                              formData.businessNumber &&
                              formData.storeName &&
                              formData.storeAddress &&
                              formData.storePhoneNumber &&
                              formData.agreeToTerms;
  
  // 채워진 필드들에만 에러가 없는지 확인
  const noErrorsInFilledFields = !Object.entries(errors).some(([field, error]) => {
    if (field === 'agreeToTerms') return false; // 약관 동의는 별도 체크
    if (field === 'storeAddressDetail') {
      // 선택 필드는 채워져 있을 때만 에러 체크
      return formData[field] && error;
    }
    // 필수 필드는 에러가 있으면 안됨
    return error;
  });
  
  return requiredFieldsFilled && noErrorsInFilledFields;
});

// 필드별 유효성 검증
const validateField = (fieldName) => {
  errors[fieldName] = '';

  switch (fieldName) {
    case 'email':
      if (!formData.email) {
        errors.email = '이메일은 필수 입력 값입니다.';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
        errors.email = '올바른 이메일 형식이 아닙니다.';
      }
      break;

    case 'password':
      if (!formData.password) {
        errors.password = '비밀번호는 필수 입력 값입니다.';
      } else if (formData.password.length < 8) {
        errors.password = '비밀번호는 8자 이상이어야 합니다.';
      } else if (!/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]/.test(formData.password)) {
        errors.password = '비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.';
      }
      break;

    case 'confirmPassword':
      if (!formData.confirmPassword) {
        errors.confirmPassword = '비밀번호 확인은 필수 입력 값입니다.';
      } else if (formData.password !== formData.confirmPassword) {
        errors.confirmPassword = '비밀번호가 일치하지 않습니다.';
      }
      break;

    case 'nickname':
      if (!formData.nickname) {
        errors.nickname = '닉네임은 필수 입력 값입니다.';
      } else if (formData.nickname.length < 2 || formData.nickname.length > 20) {
        errors.nickname = '닉네임은 2자 이상 20자 이하로 입력해주세요.';
      }
      break;

    case 'phoneNumber':
      if (!formData.phoneNumber) {
        errors.phoneNumber = '전화번호는 필수 입력 값입니다.';
      } else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(formData.phoneNumber)) {
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

    case 'storeName':
      if (!formData.storeName) {
        errors.storeName = '가게명은 필수 입력 값입니다.';
      } else if (formData.storeName.length > 100) {
        errors.storeName = '가게명은 최대 100자까지 입력 가능합니다.';
      }
      break;

    case 'storeAddress':
      if (!formData.storeAddress) {
        errors.storeAddress = '가게 주소는 필수 입력 값입니다.';
      } else if (formData.storeAddress.length > 255) {
        errors.storeAddress = '가게 주소는 최대 255자까지 입력 가능합니다.';
      }
      break;

    case 'storeAddressDetail':
      if (formData.storeAddressDetail && formData.storeAddressDetail.length > 100) {
        errors.storeAddressDetail = '상세 주소는 최대 100자까지 입력 가능합니다.';
      }
      break;

    case 'storePhoneNumber':
      if (!formData.storePhoneNumber) {
        errors.storePhoneNumber = '가게 전화번호는 필수 입력 값입니다.';
      } else if (!/^\d{2,3}-\d{3,4}-\d{4}$/.test(formData.storePhoneNumber)) {
        errors.storePhoneNumber = '가게 전화번호는 올바른 형식으로 입력해주세요. (예: 02-1234-5678)';
      }
      break;
  }
};

// 전화번호 자동 하이픈 추가
const formatPhoneNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, '');
  
  if (value.length <= 3) {
    formData.phoneNumber = value;
  } else if (value.length <= 7) {
    if (value.startsWith('02')) {
      formData.phoneNumber = value.slice(0, 2) + '-' + value.slice(2);
    } else {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3);
    }
  } else {
    if (value.startsWith('02')) {
      formData.phoneNumber = value.slice(0, 2) + '-' + value.slice(2, 6) + '-' + value.slice(6, 10);
    } else {
      formData.phoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
    }
  }
};

// 사업자등록번호 자동 하이픈 추가
const formatBusinessNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, '');
  
  if (value.length <= 3) {
    formData.businessNumber = value;
  } else if (value.length <= 5) {
    formData.businessNumber = value.slice(0, 3) + '-' + value.slice(3);
  } else {
    formData.businessNumber = value.slice(0, 3) + '-' + value.slice(3, 5) + '-' + value.slice(5, 10);
  }
};

// 가게 전화번호 자동 하이픈 추가
const formatStorePhoneNumber = (event) => {
  let value = event.target.value.replace(/[^0-9]/g, '');
  
  if (value.length <= 3) {
    formData.storePhoneNumber = value;
  } else if (value.length <= 7) {
    if (value.startsWith('02')) {
      formData.storePhoneNumber = value.slice(0, 2) + '-' + value.slice(2);
    } else {
      formData.storePhoneNumber = value.slice(0, 3) + '-' + value.slice(3);
    }
  } else {
    if (value.startsWith('02')) {
      formData.storePhoneNumber = value.slice(0, 2) + '-' + value.slice(2, 6) + '-' + value.slice(6, 10);
    } else {
      formData.storePhoneNumber = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
    }
  }
};

// 주소 검색 기능
const openAddressSearch = () => {
  if (window.daum && window.daum.Postcode) {
    new window.daum.Postcode({
      oncomplete: function(data) {
        // 선택된 주소 정보 처리
        let addr = '';
        
        if (data.userSelectedType === 'R') {
          addr = data.roadAddress;
        } else {
          addr = data.jibunAddress;
        }
        
        // 상세주소가 있는 경우 추가
        if (data.buildingName !== '') {
          addr += (data.buildingName.charAt(data.buildingName.length-1) === '동' ? '' : '동') + ' ' + data.buildingName;
        }
        
        formData.storeAddress = addr;
        validateField('storeAddress');
      },
      theme: {
        bgColor: "#FFFFFF",
        searchBgColor: "#FF1744",
        contentBgColor: "#FFFFFF",
        pageBgColor: "#FFFFFF",
        textColor: "#333333",
        queryTextColor: "#FFFFFF"
      },
      width: '100%',
      height: '100%'
    }).open();
  } else {
    // Daum 우편번호 서비스가 로드되지 않은 경우 대체 방법
    showToast('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.', 'warning');
    loadDaumPostcode();
  }
};

// Daum 우편번호 서비스 로드
const loadDaumPostcode = () => {
  const script = document.createElement('script');
  script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
  script.onload = () => {
    showToast('주소 검색 서비스가 준비되었습니다.', 'success');
  };
  document.head.appendChild(script);
};

// 폼 제출
const handleSubmit = async () => {
  // 모든 필드 유효성 검증
  Object.keys(errors).forEach(field => {
    if (field !== 'agreeToTerms') {
      validateField(field);
    }
  });

  // 약관 동의 체크
  if (!formData.agreeToTerms) {
    errors.agreeToTerms = '이용약관에 동의해주세요.';
  }

  // 필수 필드 누락 체크
  const missingFields = [];
  if (!formData.email) missingFields.push('이메일');
  if (!formData.password) missingFields.push('비밀번호');
  if (!formData.confirmPassword) missingFields.push('비밀번호 확인');
  if (!formData.nickname) missingFields.push('닉네임');
  if (!formData.phoneNumber) missingFields.push('전화번호');
  if (!formData.businessNumber) missingFields.push('사업자등록번호');
  if (!formData.storeName) missingFields.push('가게명');
  if (!formData.storeAddress) missingFields.push('가게 주소');
  if (!formData.storePhoneNumber) missingFields.push('가게 전화번호');
  if (!formData.agreeToTerms) missingFields.push('이용약관 동의');

  if (missingFields.length > 0) {
    showToast(`다음 필수 항목을 입력해주세요: ${missingFields.join(', ')}`, 'warning');
    return;
  }

  if (!isFormValid.value) {
    showToast('입력 정보를 확인해주세요.', 'warning');
    return;
  }

  isLoading.value = true;

  try {
    const requestData = {
      email: formData.email,
      password: formData.password,
      nickname: formData.nickname,
      phoneNumber: formData.phoneNumber,
      businessNumber: formData.businessNumber,
      storeName: formData.storeName,
      storeAddress: formData.storeAddress,
      storeAddressDetail: formData.storeAddressDetail || null,
      storePhoneNumber: formData.storePhoneNumber
    };

    const response = await apiClient.post('/owner/signup', requestData);
    
    // 성공 알림
    const alert = await alertController.create({
      header: '가입 완료',
      message: '사장님 계정이 성공적으로 생성되었습니다!\n승인 후 서비스를 이용하실 수 있습니다.',
      buttons: [
        {
          text: '확인',
          handler: () => {
            router.push('/owner/login');
          }
        }
      ]
    });
    await alert.present();

  } catch (error) {
    console.error('사장님 회원가입 실패:', error);
    
    let errorMessage = '회원가입 중 오류가 발생했습니다.';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.message.includes('이미 가입된 이메일')) {
      errorMessage = '이미 가입된 이메일입니다.';
    } else if (error.message.includes('이미 사용 중인 닉네임')) {
      errorMessage = '이미 사용 중인 닉네임입니다.';
    }
    
    showToast(errorMessage, 'danger');
  } finally {
    isLoading.value = false;
  }
};

// 로그인 페이지로 이동
const goToLogin = () => {
  router.push('/owner/login');
};

// 일반 회원가입 페이지로 이동
const goToCustomerSignUp = () => {
  router.push('/auth/signup');
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
.owner-signup-content {
  --background: var(--ion-color-light);
}

.signup-header {
  background: linear-gradient(135deg, #ff1744 0%, #e91e63 100%);
  color: white;
  padding: 2rem 1rem 3rem;
  text-align: center;
}

.logo-section {
  max-width: 400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.logo-text {
  font-size: 2rem;
  font-weight: 900;
  color: white;
}

.owner-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 600;
}

.signup-header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
}

.subtitle {
  margin: 0;
  font-size: 1rem;
  opacity: 0.9;
  color: white;
}

.signup-form {
  max-width: 600px;
  margin: -2rem auto 0;
  padding: 0 1rem 2rem;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.section-title {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #ff1744;
  border-bottom: 2px solid #ff1744;
  padding-bottom: 0.5rem;
}

.form-item {
  margin-bottom: 1rem;
  --border-color: #e0e0e0;
  --highlight-color: #ff1744;
}

.form-item:last-child {
  margin-bottom: 0;
}

.address-item {
  display: flex;
  align-items: center;
}

.address-search-btn {
  --color: #ff1744;
  --border-color: #ff1744;
  font-size: 0.9rem;
  height: 36px;
  margin-left: 0.5rem;
}

.required {
  color: #ff1744;
}

.error-message {
  color: var(--ion-color-danger);
  font-size: 0.875rem;
  margin: 0.25rem 0 0.5rem 1rem;
}

.terms-section {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.terms-label {
  flex: 1;
  font-size: 0.9rem;
  line-height: 1.4;
  color: #333333;
  cursor: pointer;
}

.signup-button {
  --background: #ff1744;
  --background-hover: #d50000;
  --color: white;
  font-weight: 600;
  height: 56px;
  margin-bottom: 2rem;
  border-radius: 8px;
}

.signup-button:disabled {
  --background: #cccccc;
  --color: #666666;
}

.bottom-links {
  text-align: center;
  margin-bottom: 2rem;
  padding: 1rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.bottom-links p {
  margin: 0.5rem 0;
  color: #666666;
  font-size: 0.9rem;
}

.link {
  color: #ff1744;
  cursor: pointer;
  font-weight: 600;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
  color: #d50000;
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .signup-header {
    padding: 1.5rem 1rem 2.5rem;
  }
  
  .signup-header h1 {
    font-size: 1.3rem;
  }
  
  .subtitle {
    font-size: 0.9rem;
  }
  
  .form-section {
    margin-bottom: 1rem;
    border-radius: 8px;
    padding: 1rem;
  }
  
  .section-title {
    font-size: 1.1rem;
  }
}

@media (min-width: 768px) {
  .signup-form {
    padding: 0 2rem 2rem;
  }
}
</style> 