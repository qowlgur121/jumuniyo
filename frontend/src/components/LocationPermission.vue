<template>
  <div class="location-permission">
    <!-- 위치 권한 상태 표시 -->
    <div v-if="showStatus" class="permission-status">
      <ion-item class="status-item" :class="`status-${permissionStatusClass}`">
        <ion-icon 
          :icon="statusIcon" 
          slot="start" 
          :color="statusColor"
        ></ion-icon>
        <ion-label>
          <h3>{{ statusTitle }}</h3>
          <p>{{ statusMessage }}</p>
        </ion-label>
        <ion-badge 
          slot="end" 
          :color="statusColor"
        >
          {{ statusBadge }}
        </ion-badge>
      </ion-item>
    </div>

    <!-- 위치 권한 요청 버튼 -->
    <div v-if="showRequestButton" class="permission-actions">
      <ion-button 
        expand="block" 
        color="primary"
        @click="handleRequestLocation"
        :disabled="isLoading"
      >
        <ion-icon :icon="locationOutline" slot="start"></ion-icon>
        {{ isLoading ? '위치 정보 요청 중...' : '현재 위치 사용하기' }}
      </ion-button>
    </div>

    <!-- 주소 입력 옵션 -->
    <div v-if="showAddressInput" class="address-input">
      <ion-item>
        <ion-label position="stacked">주소 직접 입력</ion-label>
        <ion-input
          v-model="addressInput"
          placeholder="예: 강남구, 서초구, 마포구"
          @ionBlur="handleAddressInput"
        ></ion-input>
      </ion-item>
      <ion-button 
        v-if="addressInput"
        fill="clear" 
        size="small"
        @click="setLocationByAddress"
        :disabled="isLoading"
      >
        <ion-icon :icon="searchOutline" slot="start"></ion-icon>
        주소로 위치 설정
      </ion-button>
    </div>

    <!-- 현재 위치 정보 표시 -->
    <div v-if="showLocationInfo && locationState.hasLocation" class="location-info">
      <ion-card>
        <ion-card-header>
          <ion-card-title>현재 위치</ion-card-title>
        </ion-card-header>
        <ion-card-content>
          <div class="location-details">
            <div class="location-address">
              <ion-icon :icon="locationOutline" color="primary"></ion-icon>
              <span>{{ locationState.formattedAddress }}</span>
            </div>
            <div v-if="showCoordinates" class="location-coordinates">
              <p>위도: {{ currentLocation?.latitude?.toFixed(6) }}</p>
              <p>경도: {{ currentLocation?.longitude?.toFixed(6) }}</p>
            </div>
            <div v-if="locationState.accuracy" class="location-accuracy">
              <ion-badge color="medium">정확도: {{ locationState.accuracy }}m</ion-badge>
            </div>
          </div>
        </ion-card-content>
      </ion-card>
    </div>

    <!-- 에러 메시지 -->
    <ion-alert
      :is-open="!!error"
      header="위치 정보 오류"
      :message="error?.message"
      :buttons="alertButtons"
      @didDismiss="clearError"
    ></ion-alert>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  IonItem,
  IonLabel,
  IonIcon,
  IonBadge,
  IonButton,
  IonInput,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonAlert
} from '@ionic/vue';
import {
  locationOutline,
  checkmarkCircleOutline,
  closeCircleOutline,
  warningOutline,
  searchOutline
} from 'ionicons/icons';
import { useLocation } from '@/composables/useLocation';

// Props 정의
const props = defineProps({
  showStatus: {
    type: Boolean,
    default: true
  },
  showRequestButton: {
    type: Boolean,
    default: true
  },
  showAddressInput: {
    type: Boolean,
    default: true
  },
  showLocationInfo: {
    type: Boolean,
    default: true
  },
  showCoordinates: {
    type: Boolean,
    default: false
  },
  autoRequest: {
    type: Boolean,
    default: false
  }
});

// Emits 정의
const emit = defineEmits(['locationChanged', 'permissionChanged', 'error']);

// 위치 정보 컴포저블 사용
const {
  currentLocation,
  permissionStatus,
  isLoading,
  error,
  locationState,
  requestCurrentLocation,
  setLocationByAddress: setLocationByAddressComposable,
  refreshLocation
} = useLocation();

// 로컬 상태
const addressInput = ref('');

// 계산된 속성들
const permissionStatusClass = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return 'granted';
    case 'denied':
      return 'denied';
    case 'prompt':
      return 'prompt';
    default:
      return 'unknown';
  }
});

const statusIcon = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return checkmarkCircleOutline;
    case 'denied':
      return closeCircleOutline;
    case 'prompt':
      return warningOutline;
    default:
      return locationOutline;
  }
});

const statusColor = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return 'success';
    case 'denied':
      return 'danger';
    case 'prompt':
      return 'warning';
    default:
      return 'medium';
  }
});

const statusTitle = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return '위치 권한 허용됨';
    case 'denied':
      return '위치 권한 거부됨';
    case 'prompt':
      return '위치 권한 필요';
    default:
      return '위치 권한 상태 확인 중';
  }
});

const statusMessage = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return '현재 위치를 사용할 수 있습니다.';
    case 'denied':
      return '주소를 직접 입력해서 위치를 설정해주세요.';
    case 'prompt':
      return '더 정확한 서비스를 위해 위치 권한을 허용해주세요.';
    default:
      return '위치 서비스 상태를 확인하고 있습니다.';
  }
});

const statusBadge = computed(() => {
  switch (permissionStatus.value) {
    case 'granted':
      return '허용';
    case 'denied':
      return '거부';
    case 'prompt':
      return '대기';
    default:
      return '확인중';
  }
});

// 알림 버튼 설정
const alertButtons = computed(() => [
  {
    text: '확인',
    role: 'confirm',
    handler: () => clearError()
  },
  ...(error.value?.code === 'PERMISSION_DENIED' ? [{
    text: '주소 입력',
    handler: () => {
      clearError();
      // 주소 입력 섹션으로 스크롤 또는 포커스
    }
  }] : [])
]);

// 메서드들
const handleRequestLocation = async () => {
  try {
    const location = await requestCurrentLocation();
    if (location) {
      emit('locationChanged', location);
    }
  } catch (err) {
    emit('error', err);
  }
};

const handleAddressInput = () => {
  if (addressInput.value && addressInput.value.length > 1) {
    setLocationByAddress();
  }
};

const setLocationByAddress = async () => {
  if (!addressInput.value) return;

  try {
    const location = await setLocationByAddressComposable(addressInput.value);
    if (location) {
      emit('locationChanged', location);
      addressInput.value = ''; // 성공 시 입력 필드 초기화
    }
  } catch (err) {
    emit('error', err);
  }
};

const clearError = () => {
  // error는 composable에서 관리되므로 여기서는 이벤트만 발생
  emit('error', null);
};

// 위치 정보 변화 감시
watch(currentLocation, (newLocation) => {
  if (newLocation) {
    emit('locationChanged', newLocation);
  }
});

// 권한 상태 변화 감시
watch(permissionStatus, (newStatus) => {
  emit('permissionChanged', newStatus);
});

// 자동 위치 요청
if (props.autoRequest && locationState.canRequestLocation) {
  requestCurrentLocation();
}
</script>

<style scoped>
.location-permission {
  padding: 16px;
}

.permission-status {
  margin-bottom: 16px;
}

.status-item {
  --background: var(--ion-color-light);
  --border-radius: 8px;
  margin-bottom: 8px;
}

.status-item.status-granted {
  --background: var(--ion-color-success-tint);
}

.status-item.status-denied {
  --background: var(--ion-color-danger-tint);
}

.status-item.status-prompt {
  --background: var(--ion-color-warning-tint);
}

.permission-actions {
  margin-bottom: 16px;
}

.address-input {
  margin-bottom: 16px;
}

.address-input ion-item {
  --background: var(--ion-color-light);
  --border-radius: 8px;
  margin-bottom: 8px;
}

.location-info {
  margin-top: 16px;
}

.location-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.location-address {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.location-coordinates {
  font-size: 0.9em;
  color: var(--ion-color-medium);
}

.location-coordinates p {
  margin: 4px 0;
}

.location-accuracy {
  display: flex;
  justify-content: flex-end;
}
</style> 