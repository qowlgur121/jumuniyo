<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>음식점 관리</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="refreshData" :disabled="isLoading">
            <ion-icon :icon="refreshOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="dashboard-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="dashboard-header">
          <h1 class="responsive-title">음식점 관리 대시보드</h1>
          <p class="subtitle">음식점 운영에 필요한 모든 것을 한곳에서 관리하세요</p>
        </div>

        <!-- 음식점 선택 -->
        <div class="form-section" v-if="stores.length > 1">
          <ion-item class="store-selector">
            <ion-select
              v-model="selectedStoreId"
              placeholder="음식점을 선택하세요"
              interface="action-sheet"
              @ionChange="onStoreChange"
            >
              <div slot="label">
                <ion-icon :icon="storefront"></ion-icon>
                관리할 음식점
              </div>
              <ion-select-option
                v-for="store in stores"
                :key="store.id"
                :value="store.id"
              >
                {{ store.name }}
              </ion-select-option>
            </ion-select>
          </ion-item>
        </div>

        <!-- 대시보드 메인 콘텐츠 -->
        <div v-if="selectedStore" class="dashboard-main">
          <!-- 음식점 기본 정보 카드 -->
          <div class="management-card store-info-card">
            <div class="card-header">
              <div class="card-title">
                <ion-icon :icon="restaurantOutline" class="card-icon"></ion-icon>
                <h2>음식점 정보</h2>
              </div>
              <ion-button 
                fill="clear" 
                size="small" 
                @click="editStoreInfo"
                class="action-button"
              >
                <ion-icon :icon="pencilOutline"></ion-icon>
              </ion-button>
            </div>
            <div class="card-content">
              <div class="store-summary">
                <div class="store-name">{{ selectedStore.name }}</div>
                <div class="store-details">
                  <div class="detail-item">
                    <ion-icon :icon="locationOutline"></ion-icon>
                    <span>{{ selectedStore.address || '주소 미입력' }}</span>
                  </div>
                  <div class="detail-item">
                    <ion-icon :icon="callOutline"></ion-icon>
                    <span>{{ selectedStore.phoneNumber || '전화번호 미입력' }}</span>
                  </div>
                  <div class="detail-item">
                    <ion-icon :icon="fastFoodOutline"></ion-icon>
                    <span>{{ selectedStore.category || '카테고리 미설정' }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="status-badge" :class="getStatusClass(selectedStore.status)">
                      {{ getStatusText(selectedStore.status) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 운영시간 카드 -->
          <div class="management-card operating-hours-card">
            <div class="card-header">
              <div class="card-title">
                <ion-icon :icon="timeOutline" class="card-icon"></ion-icon>
                <h2>운영시간</h2>
              </div>
              <ion-button 
                fill="clear" 
                size="small" 
                @click="manageOperatingHours"
                class="action-button"
              >
                <ion-icon :icon="settingsOutline"></ion-icon>
              </ion-button>
            </div>
            <div class="card-content">
              <div class="hours-summary">
                <div class="today-hours">
                  <h3>오늘 ({{ getTodayName() }})</h3>
                  <div class="hours-info">
                    <span v-if="todayHours?.isOpen" class="open-status">
                      <ion-icon :icon="checkmarkCircle" color="success"></ion-icon>
                      {{ todayHours.openTime }} - {{ todayHours.closeTime }}
                    </span>
                    <span v-else class="closed-status">
                      <ion-icon :icon="closeCircle" color="danger"></ion-icon>
                      휴무일
                    </span>
                  </div>
                  <div v-if="todayHours?.hasBreakTime" class="break-time">
                    브레이크: {{ todayHours.breakStartTime }} - {{ todayHours.breakEndTime }}
                  </div>
                </div>
                <div class="hours-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ operatingStats.openDays }}/7</span>
                    <span class="stat-label">영업일</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ operatingStats.avgHours }}시간</span>
                    <span class="stat-label">평균 영업</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 배달지역 카드 -->
          <div class="management-card delivery-areas-card">
            <div class="card-header">
              <div class="card-title">
                <ion-icon :icon="mapOutline" class="card-icon"></ion-icon>
                <h2>배달지역</h2>
              </div>
              <ion-button 
                fill="clear" 
                size="small" 
                @click="manageDeliveryAreas"
                class="action-button"
              >
                <ion-icon :icon="addOutline"></ion-icon>
              </ion-button>
            </div>
            <div class="card-content">
              <div class="delivery-summary">
                <div class="areas-count">
                  <h3>등록된 배달지역</h3>
                  <div class="count-display">
                    <span class="count-number">{{ deliveryAreas.length }}</span>
                    <span class="count-label">개 지역</span>
                  </div>
                </div>
                <div class="areas-stats" v-if="deliveryAreas.length > 0">
                  <div class="stat-item">
                    <span class="stat-value">{{ formatCurrency(deliveryStats.minFee) }}</span>
                    <span class="stat-label">최소 배달비</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ formatCurrency(deliveryStats.maxFee) }}</span>
                    <span class="stat-label">최고 배달비</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ deliveryStats.avgTime }}분</span>
                    <span class="stat-label">평균 배달시간</span>
                  </div>
                </div>
                <div v-if="deliveryAreas.length > 0" class="active-areas">
                  <div 
                    v-for="area in deliveryAreas.slice(0, 3)" 
                    :key="area.id"
                    class="area-chip"
                    :class="{ 'area-inactive': !area.isActive }"
                  >
                    {{ area.areaName }}
                  </div>
                  <div v-if="deliveryAreas.length > 3" class="more-areas">
                    +{{ deliveryAreas.length - 3 }}개 더
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 빠른 액션 버튼들 -->
          <div class="quick-actions">
            <h2 class="section-title">빠른 작업</h2>
            <div class="action-grid">
              <ion-button 
                fill="outline" 
                expand="block" 
                @click="editStoreInfo"
                class="quick-action-button"
              >
                <ion-icon :icon="pencilOutline" slot="start"></ion-icon>
                정보 수정
              </ion-button>
              <ion-button 
                fill="outline" 
                expand="block" 
                @click="manageOperatingHours"
                class="quick-action-button"
              >
                <ion-icon :icon="timeOutline" slot="start"></ion-icon>
                운영시간 관리
              </ion-button>
              <ion-button 
                fill="outline" 
                expand="block" 
                @click="manageDeliveryAreas"
                class="quick-action-button"
              >
                <ion-icon :icon="mapOutline" slot="start"></ion-icon>
                배달지역 관리
              </ion-button>
              <ion-button 
                fill="outline" 
                expand="block" 
                @click="viewMyStores"
                class="quick-action-button"
              >
                <ion-icon :icon="listOutline" slot="start"></ion-icon>
                전체 음식점
              </ion-button>
            </div>
          </div>
        </div>

        <!-- 빈 상태 (음식점 선택되지 않음) -->
        <div v-if="!selectedStoreId && stores.length > 0" class="empty-state">
          <ion-icon :icon="storefront" class="empty-icon"></ion-icon>
          <h3>음식점을 선택해주세요</h3>
          <p>관리할 음식점을 선택하여 대시보드를 시작하세요.</p>
        </div>

        <!-- 빈 상태 (등록된 음식점 없음) -->
        <div v-if="stores.length === 0" class="empty-state">
          <ion-icon :icon="addCircleOutline" class="empty-icon"></ion-icon>
          <h3>등록된 음식점이 없습니다</h3>
          <p>먼저 음식점을 등록하여 사업을 시작해보세요.</p>
          <ion-button 
            fill="outline" 
            @click="registerStore"
            class="action-button"
          >
            <ion-icon :icon="addOutline" slot="start"></ion-icon>
            음식점 등록하기
          </ion-button>
        </div>
      </div>
    </ion-content>

    <!-- 로딩 오버레이 -->
    <ion-loading
      :is-open="isLoading"
      message="데이터를 불러오는 중..."
    ></ion-loading>

    <!-- 토스트 -->
    <ion-toast
      :is-open="showToast"
      :message="toastMessage"
      :duration="3000"
      @didDismiss="showToast = false"
    ></ion-toast>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonButton,
  IonIcon,
  IonItem,
  IonSelect,
  IonSelectOption,
  IonLoading,
  IonToast
} from '@ionic/vue'
import {
  refreshOutline,
  storefront,
  restaurantOutline,
  pencilOutline,
  locationOutline,
  callOutline,
  fastFoodOutline,
  timeOutline,
  settingsOutline,
  checkmarkCircle,
  closeCircle,
  mapOutline,
  addOutline,
  addCircleOutline,
  listOutline
} from 'ionicons/icons'
import axios from 'axios'

// 타입 정의
interface Store {
  id: number
  name: string
  address?: string
  phoneNumber?: string
  category?: string
  status?: string
}

interface OperatingHour {
  dayOfWeek: string
  isOpen: boolean
  openTime?: string
  closeTime?: string
  hasBreakTime?: boolean
  breakStartTime?: string
  breakEndTime?: string
}

interface DeliveryArea {
  id: number
  areaName: string
  deliveryFee: number
  minimumOrderAmount: number
  estimatedDeliveryTime: number
  isActive: boolean
}

// 라우터
const router = useRouter()

// 상태 관리
const isLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const selectedStoreId = ref<number | null>(null)
const stores = ref<Store[]>([])
const operatingHours = ref<OperatingHour[]>([])
const deliveryAreas = ref<DeliveryArea[]>([])

// 컴포넌트 마운트 시
onMounted(async () => {
  await loadStores()
  if (stores.value.length === 1) {
    selectedStoreId.value = stores.value[0].id
    await loadDashboardData()
  }
})

// 선택된 음식점 변경 감지
watch(selectedStoreId, async (newStoreId) => {
  if (newStoreId) {
    await loadDashboardData()
  }
})

// 계산된 속성들
const selectedStore = computed(() => {
  return stores.value.find(store => store.id === selectedStoreId.value)
})

const todayHours = computed(() => {
  const today = new Date().getDay() // 0: 일요일, 1: 월요일, ...
  const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
  const todayName = dayNames[today]
  return operatingHours.value.find(hour => hour.dayOfWeek === todayName)
})

const operatingStats = computed(() => {
  const openDays = operatingHours.value.filter(hour => hour.isOpen).length
  const totalHours = operatingHours.value
    .filter(hour => hour.isOpen && hour.openTime && hour.closeTime)
    .reduce((sum, hour) => {
      const openTime = new Date(`1970-01-01T${hour.openTime}:00`)
      const closeTime = new Date(`1970-01-01T${hour.closeTime}:00`)
      const diff = (closeTime.getTime() - openTime.getTime()) / (1000 * 60 * 60) // 시간 단위
      return sum + diff
    }, 0)
  
  return {
    openDays,
    avgHours: openDays > 0 ? Math.round(totalHours / openDays) : 0
  }
})

const deliveryStats = computed(() => {
  if (deliveryAreas.value.length === 0) {
    return { minFee: 0, maxFee: 0, avgTime: 0 }
  }
  
  const fees = deliveryAreas.value.map(area => area.deliveryFee)
  const times = deliveryAreas.value.map(area => area.estimatedDeliveryTime)
  
  return {
    minFee: Math.min(...fees),
    maxFee: Math.max(...fees),
    avgTime: Math.round(times.reduce((sum, time) => sum + time, 0) / times.length)
  }
})

// 메소드들
const loadStores = async () => {
  try {
    isLoading.value = true
    // TODO: 실제 API 엔드포인트로 교체
    const response = await axios.get('/api/v1/stores/my-stores')
    stores.value = response.data || []
  } catch (error) {
    console.error('음식점 목록 로드 실패:', error)
    showToastMessage('음식점 목록을 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const loadDashboardData = async () => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    
    // 운영시간 정보 로드
    const operatingHoursResponse = await axios.get(`/api/v1/stores/${selectedStoreId.value}/operating-hours`)
    operatingHours.value = operatingHoursResponse.data || []
    
    // 배달지역 정보 로드
    const deliveryAreasResponse = await axios.get(`/api/v1/stores/${selectedStoreId.value}/delivery-areas`)
    deliveryAreas.value = deliveryAreasResponse.data || []
    
  } catch (error) {
    console.error('대시보드 데이터 로드 실패:', error)
    showToastMessage('대시보드 정보를 불러오는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const refreshData = async () => {
  await loadStores()
  if (selectedStoreId.value) {
    await loadDashboardData()
  }
  showToastMessage('데이터가 새로고침되었습니다.')
}

const onStoreChange = () => {
  loadDashboardData()
}

const getTodayName = () => {
  const today = new Date().getDay()
  const dayNames = ['일요일', '월요일', '화요일', '수요일', '목요일', '금요일', '토요일']
  return dayNames[today]
}

const getStatusClass = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'status-active'
    case 'INACTIVE': return 'status-inactive'
    case 'PENDING': return 'status-pending'
    default: return 'status-unknown'
  }
}

const getStatusText = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return '운영중'
    case 'INACTIVE': return '휴업중'
    case 'PENDING': return '승인대기'
    default: return '상태불명'
  }
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

// 내비게이션 메소드들
const editStoreInfo = () => {
  router.push('/store/register')
}

const manageOperatingHours = () => {
  router.push('/store/operating-hours')
}

const manageDeliveryAreas = () => {
  router.push('/store/delivery-areas')
}

const viewMyStores = () => {
  router.push('/store/my')
}

const registerStore = () => {
  router.push('/store/register')
}

const showToastMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}
</script>

<style scoped>
.dashboard-content {
  --background: #f8f9fa;
}

.responsive-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
}

.responsive-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.store-selector {
  --background: #f8f9fa;
  --border-radius: 8px;
}

.dashboard-main {
  display: grid;
  gap: 20px;
}

.management-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.management-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e9ecef;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-title h2 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.card-icon {
  font-size: 1.5rem;
  color: #007bff;
}

.action-button {
  --padding-start: 8px;
  --padding-end: 8px;
}

.card-content {
  margin-top: 10px;
}

/* 음식점 정보 카드 */
.store-summary .store-name {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 15px;
}

.store-details {
  display: grid;
  gap: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

.detail-item ion-icon {
  font-size: 1.1rem;
  color: #007bff;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  text-align: center;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-unknown {
  background: #e2e3e5;
  color: #383d41;
}

/* 운영시간 카드 */
.hours-summary {
  display: grid;
  gap: 20px;
}

.today-hours h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.hours-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.open-status,
.closed-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 600;
}

.open-status {
  color: #28a745;
}

.closed-status {
  color: #dc3545;
}

.break-time {
  font-size: 0.9rem;
  color: #666;
  padding-left: 24px;
}

.hours-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #007bff;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

/* 배달지역 카드 */
.delivery-summary {
  display: grid;
  gap: 20px;
}

.areas-count h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.count-display {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.count-number {
  font-size: 2rem;
  font-weight: 700;
  color: #007bff;
}

.count-label {
  color: #666;
}

.areas-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 15px;
}

.active-areas {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.area-chip {
  padding: 6px 12px;
  background: #007bff;
  color: white;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.area-inactive {
  background: #6c757d;
}

.more-areas {
  padding: 6px 12px;
  background: #e9ecef;
  color: #495057;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

/* 빠른 액션 */
.quick-actions {
  margin-top: 30px;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 15px;
}

.quick-action-button {
  --border-radius: 12px;
  height: 50px;
  font-weight: 600;
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  color: #adb5bd;
}

.empty-state h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.empty-state p {
  margin: 0 0 20px 0;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .responsive-container {
    padding: 15px;
  }
  
  .hours-stats {
    gap: 20px;
  }
  
  .areas-stats {
    grid-template-columns: 1fr;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
  }
}

@media (min-width: 769px) {
  .dashboard-main {
    grid-template-columns: 1fr 1fr;
  }
  
  .store-info-card {
    grid-column: 1 / -1;
  }
  
  .quick-actions {
    grid-column: 1 / -1;
  }
}

@media (min-width: 1024px) {
  .dashboard-main {
    grid-template-columns: 2fr 1fr 1fr;
  }
  
  .store-info-card {
    grid-column: 1 / -1;
  }
  
  .operating-hours-card {
    grid-column: 1 / 2;
  }
  
  .delivery-areas-card {
    grid-column: 2 / 3;
  }
  
  .quick-actions {
    grid-column: 1 / -1;
  }
}
</style> 