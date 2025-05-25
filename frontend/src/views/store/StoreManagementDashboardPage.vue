<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
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
          <h1 class="responsive-title">가게 관리</h1>
          <p class="subtitle" v-if="selectedStore">{{ selectedStore.name }} 관리 대시보드</p>
          <p class="subtitle" v-else>관리할 가게를 선택하세요</p>
        </div>

        <!-- 음식점 선택 카드 -->
        <div class="store-selector-card" v-if="stores.length > 0">
          <div class="selector-header">
            <div class="selector-title">
              <ion-icon :icon="storefront" class="selector-icon"></ion-icon>
              <h2>가게 선택</h2>
            </div>
            <ion-button 
              fill="outline" 
              size="small"
              @click="registerStore"
            >
              <ion-icon :icon="addOutline" slot="start"></ion-icon>
              가게 추가
            </ion-button>
          </div>
          
          <div class="store-grid">
            <div 
              v-for="store in stores"
              :key="store.id"
              class="store-card"
              :class="{ 'store-selected': selectedStoreId === store.id }"
              @click="selectStore(store.id)"
            >
              <div class="store-card-header">
                <div class="store-info">
                  <h3 class="store-name">{{ store.name }}</h3>
                  <p class="store-category">{{ getCategoryName(store.category) }}</p>
                </div>
                <div class="store-status">
                  <span class="status-badge" :class="getStatusClass(store.status)">
                    {{ getStatusText(store.status) }}
                  </span>
                </div>
              </div>
              
              <div class="store-card-content">
                <div class="store-detail">
                  <ion-icon :icon="locationOutline" class="detail-icon"></ion-icon>
                  <span class="detail-text">{{ store.address || '주소 미입력' }}</span>
                </div>
                <div class="store-detail">
                  <ion-icon :icon="callOutline" class="detail-icon"></ion-icon>
                  <span class="detail-text">{{ store.phoneNumber || '전화번호 미입력' }}</span>
                </div>
              </div>
              
              <div class="store-card-actions">
                <ion-button 
                  fill="clear" 
                  size="small"
                  @click.stop="selectAndEdit(store.id)"
                >
                  <ion-icon :icon="pencilOutline"></ion-icon>
                </ion-button>
                <ion-button 
                  fill="clear" 
                  size="small"
                  @click.stop="viewStoreDetails(store.id)"
                >
                  <ion-icon :icon="eyeOutline"></ion-icon>
                </ion-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 선택된 가게 상세 정보 -->
        <div v-if="selectedStore" class="selected-store-dashboard">
          <!-- 가게 요약 정보 -->
          <div class="store-summary-card">
            <div class="summary-header">
              <div class="store-main-info">
                <h2 class="store-title">{{ selectedStore.name }}</h2>
                <div class="store-meta">
                  <span class="category-tag">{{ getCategoryName(selectedStore.category) }}</span>
                  <span class="status-indicator" :class="getStatusClass(selectedStore.status)">
                    {{ getStatusText(selectedStore.status) }}
                  </span>
                </div>
              </div>
              <div class="summary-actions">
                <ion-button 
                  fill="outline" 
                  size="small"
                  @click="editStoreInfo"
                >
                  <ion-icon :icon="pencilOutline" slot="start"></ion-icon>
                  정보 수정
                </ion-button>
              </div>
            </div>
            
            <div class="summary-content">
              <div class="info-grid">
                <div class="info-item">
                  <ion-icon :icon="locationOutline" class="info-icon"></ion-icon>
                  <div class="info-content">
                    <span class="info-label">주소</span>
                    <span class="info-value">{{ selectedStore.address || '주소 미입력' }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <ion-icon :icon="callOutline" class="info-icon"></ion-icon>
                  <div class="info-content">
                    <span class="info-label">전화번호</span>
                    <span class="info-value">{{ selectedStore.phoneNumber || '전화번호 미입력' }}</span>
                  </div>
                </div>
                <div class="info-item">
                  <ion-icon :icon="timeOutline" class="info-icon"></ion-icon>
                  <div class="info-content">
                    <span class="info-label">오늘 운영시간</span>
                    <span class="info-value" v-if="todayHours?.isOpen">
                      {{ todayHours.openTime }} - {{ todayHours.closeTime }}
                    </span>
                    <span class="info-value closed" v-else>휴무일</span>
                  </div>
                </div>
                <div class="info-item">
                  <ion-icon :icon="mapOutline" class="info-icon"></ion-icon>
                  <div class="info-content">
                    <span class="info-label">배달지역</span>
                    <span class="info-value">{{ deliveryAreas.length }}개 지역</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 관리 메뉴 -->
          <div class="management-menu">
            <h3 class="menu-title">가게 관리</h3>
            <div class="menu-grid">
              <div class="menu-item" @click="editStoreInfo">
                <div class="menu-icon-wrapper">
                  <ion-icon :icon="restaurantOutline" class="menu-icon"></ion-icon>
                </div>
                <div class="menu-content">
                  <h4 class="menu-name">기본 정보</h4>
                  <p class="menu-desc">가게 정보, 운영시간 수정</p>
                </div>
                <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
              </div>
              
              <div class="menu-item" @click="manageDeliveryAreas">
                <div class="menu-icon-wrapper">
                  <ion-icon :icon="mapOutline" class="menu-icon"></ion-icon>
                </div>
                <div class="menu-content">
                  <h4 class="menu-name">배달지역 관리</h4>
                  <p class="menu-desc">배달 가능 지역 설정</p>
                </div>
                <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
              </div>
              
              <div class="menu-item" @click="viewMyStores">
                <div class="menu-icon-wrapper">
                  <ion-icon :icon="listOutline" class="menu-icon"></ion-icon>
                </div>
                <div class="menu-content">
                  <h4 class="menu-name">전체 가게 목록</h4>
                  <p class="menu-desc">모든 가게 한눈에 보기</p>
                </div>
                <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
              </div>
              
              <div class="menu-item" @click="registerStore">
                <div class="menu-icon-wrapper">
                  <ion-icon :icon="addCircleOutline" class="menu-icon"></ion-icon>
                </div>
                <div class="menu-content">
                  <h4 class="menu-name">새 가게 등록</h4>
                  <p class="menu-desc">새로운 가게 추가하기</p>
                </div>
                <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
              </div>
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
  listOutline,
  eyeOutline,
  chevronForwardOutline
} from 'ionicons/icons'
// @ts-ignore
import apiClient from '@/services/api'

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
const categories = ref<any[]>([])

// 컴포넌트 마운트 시
onMounted(async () => {
  await loadStores()
  await loadCategories()
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

// 메소들
const loadCategories = async () => {
  try {
    const response = await apiClient.get('/categories')
    categories.value = response.data || []
  } catch (error) {
    console.error('카테고리 로드 실패:', error)
  }
}

const getCategoryName = (categoryId: any) => {
  const category = categories.value.find((cat: any) => cat.id === categoryId)
  return category ? category.name : '카테고리 없음'
}

const loadStores = async () => {
  try {
    isLoading.value = true
    // 실제 API 엔드포인트로 수정
    const response = await apiClient.get('/stores/my')
    stores.value = response.data.content || response.data || []
    console.log('로드된 가게 목록:', stores.value)
    
    // 가게가 없을 때 안내 메시지
    if (stores.value.length === 0) {
      showToastMessage('등록된 가게가 없습니다. 새로운 가게를 등록해보세요!')
    }
  } catch (error: any) {
    console.error('음식점 목록 로드 실패:', error)
    
    // 인증 오류인 경우
    if (error.response?.status === 401) {
      showToastMessage('로그인이 필요합니다. 다시 로그인해주세요.')
      router.push('/owner/login')
      return
    }
    
    // 기타 오류
    showToastMessage('음식점 목록을 불러오는데 실패했습니다. 새로고침을 시도해보세요.')
    // 에러 발생 시 빈 배열로 설정
    stores.value = []
  } finally {
    isLoading.value = false
  }
}

const loadDashboardData = async () => {
  if (!selectedStoreId.value) return
  
  try {
    isLoading.value = true
    
    // 운영시간 정보 로드 (API 경로 수정)
    try {
      const operatingHoursResponse = await apiClient.get(`/stores/${selectedStoreId.value}/operating-hours`)
      operatingHours.value = operatingHoursResponse.data || []
    } catch (error) {
      console.warn('운영시간 정보 로드 실패:', error)
      operatingHours.value = []
    }
    
    // 배달지역 정보 로드 (API 경로 수정)
    try {
      const deliveryAreasResponse = await apiClient.get(`/stores/${selectedStoreId.value}/delivery-areas`)
      deliveryAreas.value = deliveryAreasResponse.data || []
    } catch (error) {
      console.warn('배달지역 정보 로드 실패:', error)
      deliveryAreas.value = []
    }
    
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
  if (selectedStoreId.value) {
    router.push(`/store/edit/${selectedStoreId.value}`)
  } else {
    showToastMessage('수정할 음식점을 선택해주세요.')
  }
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

const selectStore = (storeId: number) => {
  selectedStoreId.value = storeId
}

const viewStoreDetails = (storeId: number) => {
  router.push(`/store/details/${storeId}`)
}

const selectAndEdit = (storeId: number) => {
  selectedStoreId.value = storeId
  editStoreInfo()
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

/* 가게 선택 카드 스타일 */
.store-selector-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.selector-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selector-title h2 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.selector-icon {
  font-size: 1.5rem;
  color: #007bff;
}

.store-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.store-card {
  background: #f8f9fa;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.store-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 16px rgba(0, 123, 255, 0.15);
  transform: translateY(-2px);
}

.store-selected {
  border-color: #007bff !important;
  background: #e7f3ff !important;
  box-shadow: 0 4px 16px rgba(0, 123, 255, 0.2) !important;
}

.store-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.store-name {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.store-category {
  font-size: 0.9rem;
  color: #6c757d;
  margin: 0;
}

.store-card-content {
  margin-bottom: 16px;
}

.store-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 0.9rem;
  color: #495057;
}

.detail-icon {
  font-size: 1rem;
  color: #007bff;
  flex-shrink: 0;
}

.detail-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.store-card-actions {
  display: flex;
  gap: 8px;
  position: absolute;
  top: 16px;
  right: 16px;
}

/* 선택된 가게 대시보드 스타일 */
.selected-store-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.store-summary-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e9ecef;
}

.store-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.store-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.category-tag {
  background: #e7f3ff;
  color: #007bff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
}

.status-indicator {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.info-icon {
  font-size: 1.2rem;
  color: #007bff;
  margin-top: 2px;
  flex-shrink: 0;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 0.9rem;
  color: #6c757d;
  font-weight: 500;
}

.info-value {
  font-size: 1rem;
  color: #1a1a1a;
  font-weight: 600;
}

.info-value.closed {
  color: #dc3545;
}

/* 관리 메뉴 스타일 */
.management-menu {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.menu-title {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
}

.menu-grid {
  display: grid;
  gap: 12px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.menu-item:hover {
  background: #e7f3ff;
  border-color: #007bff;
  transform: translateX(4px);
}

.menu-icon-wrapper {
  width: 48px;
  height: 48px;
  background: #007bff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-icon {
  font-size: 1.5rem;
  color: white;
}

.menu-content {
  flex: 1;
}

.menu-name {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.menu-desc {
  font-size: 0.9rem;
  color: #6c757d;
  margin: 0;
}

.menu-arrow {
  font-size: 1.2rem;
  color: #adb5bd;
  flex-shrink: 0;
}
</style> 