<template>
  <ion-page class="store-list-page">
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>주변 음식점</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="handleRefreshLocation">
            <ion-icon :icon="refreshOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <!-- 위치 권한 컴포넌트 -->
      <LocationPermission
        @locationChanged="handleLocationChanged"
        @permissionChanged="handlePermissionChanged"
        :showCoordinates="false"
      />

      <!-- 검색 필터 -->
      <div class="search-filters" v-if="currentLocation">
        <ion-item>
          <ion-label position="stacked">검색 키워드</ion-label>
          <ion-input
            v-model="searchParams.keyword"
            placeholder="음식점명, 음식 종류"
            @ionInput="debouncedSearch"
          ></ion-input>
        </ion-item>

        <ion-item>
          <ion-label position="stacked">반경 설정</ion-label>
          <ion-select v-model="searchParams.radiusKm" @ionChange="() => searchStores(true)">
            <ion-select-option value="1">1km</ion-select-option>
            <ion-select-option value="3">3km</ion-select-option>
            <ion-select-option value="5">5km</ion-select-option>
            <ion-select-option value="10">10km</ion-select-option>
            <ion-select-option value="20">20km</ion-select-option>
          </ion-select>
        </ion-item>

        <ion-item>
          <ion-label position="stacked">정렬 방식</ion-label>
          <ion-select v-model="searchParams.sortBy" @ionChange="() => searchStores(true)">
            <ion-select-option value="distance">거리순</ion-select-option>
            <ion-select-option value="rating">평점순</ion-select-option>
            <ion-select-option value="reviewCount">리뷰순</ion-select-option>
            <ion-select-option value="deliveryTime">배달시간순</ion-select-option>
            <ion-select-option value="deliveryFee">배달비순</ion-select-option>
          </ion-select>
        </ion-item>
      </div>

      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="loading-container">
        <ion-spinner name="crescent"></ion-spinner>
        <p>음식점을 찾고 있습니다...</p>
      </div>

      <!-- 음식점 목록 -->
      <div v-else-if="stores.length > 0" class="store-list">
        <ion-card
          v-for="store in stores"
          :key="store.id"
          @click="goToStoreDetail(store.id)"
          class="store-card"
        >
          <div class="store-image-container">
            <img
              :src="store.imageUrl || '/assets/images/store-placeholder.jpg'"
              :alt="store.name"
              class="store-image"
            >
            <div class="store-badges">
              <ion-badge v-if="store.distance" color="primary">
                {{ formatDistance(store.distance) }}
              </ion-badge>
              <ion-badge v-if="store.deliveryFee === 0" color="danger">
                무료배달
              </ion-badge>
            </div>
          </div>

          <ion-card-content>
            <div class="store-header">
              <h3 class="store-name">{{ store.name }}</h3>
              <div class="store-rating">
                <ion-icon :icon="starOutline" color="warning"></ion-icon>
                <span>{{ store.averageRating?.toFixed(1) || '0.0' }}</span>
                <span class="review-count">({{ store.reviewCount || 0 }})</span>
              </div>
            </div>

            <div class="store-info">
              <div class="info-row">
                <ion-icon :icon="timeOutline" color="medium"></ion-icon>
                <span>{{ store.estimatedDeliveryTime || '30-40' }}분</span>
              </div>
              <div class="info-row">
                <ion-icon :icon="cardOutline" color="medium"></ion-icon>
                <span>
                  최소주문 {{ formatPrice(store.minimumOrderAmount) }}
                  · 배달비 {{ formatPrice(store.deliveryFee) }}
                </span>
              </div>
              <div v-if="store.description" class="store-description">
                {{ store.description }}
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- 무한 스크롤 -->
        <ion-infinite-scroll 
          @ionInfinite="loadMoreStores" 
          threshold="100px" 
          :disabled="!hasMoreData"
        >
          <ion-infinite-scroll-content
            loading-spinner="bubbles"
            loading-text="더 많은 음식점을 불러오는 중..."
          >
          </ion-infinite-scroll-content>
        </ion-infinite-scroll>
      </div>

      <!-- 검색 결과 없음 -->
      <div v-else-if="!isLoading && currentLocation" class="no-results">
        <ion-icon :icon="searchOutline" size="large" color="medium"></ion-icon>
        <h3>주변에 음식점이 없습니다</h3>
        <p>검색 범위를 늘려보거나 다른 지역을 시도해보세요.</p>
        <ion-button fill="outline" @click="expandSearchRadius">
          검색 범위 확대
        </ion-button>
      </div>

      <!-- 위치 정보 없음 -->
      <div v-else-if="!currentLocation && !isLocationLoading" class="no-location">
        <ion-icon :icon="locationOutline" size="large" color="medium"></ion-icon>
        <h3>위치 정보가 필요합니다</h3>
        <p>주변 음식점을 찾기 위해 현재 위치를 설정해주세요.</p>
      </div>

      <!-- 에러 메시지 -->
      <ion-toast
        :is-open="!!error?.message"
        :message="error?.message"
        :duration="3000"
        position="bottom"
        color="danger"
      ></ion-toast>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonContent,
  IonItem,
  IonLabel,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonCard,
  IonCardContent,
  IonBadge,
  IonIcon,
  IonSpinner,
  IonInfiniteScroll,
  IonInfiniteScrollContent,
  IonToast
} from '@ionic/vue';
import {
  refreshOutline,
  starOutline,
  timeOutline,
  cardOutline,
  searchOutline,
  locationOutline
} from 'ionicons/icons';
import { useLocation } from '@/composables/useLocation';
import { storeApi } from '@/services/storeApi';
import LocationPermission from '@/components/LocationPermission.vue';

const router = useRouter();

// 위치 정보 컴포저블
const {
  currentLocation,
  isLoading: isLocationLoading,
  refreshLocation
} = useLocation();

// 반응형 상태
const stores = ref([]);
const isLoading = ref(false);
const isLoadingMore = ref(false);
const error = ref(null);

// 검색 파라미터
const searchParams = reactive({
  keyword: '',
  radiusKm: 5,
  sortBy: 'distance',
  page: 0,
  size: 20
});

// 페이지네이션 정보
const pagination = reactive({
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  hasNext: true,
  isLast: false
});

// 무한 스크롤 관련 상태
const hasMoreData = computed(() => !pagination.isLast && pagination.hasNext);

// 디바운스 타이머
let searchTimeout = null;

// 음식점 검색 함수 (초기 로드)
const searchStores = async (resetPage = true) => {
  if (!currentLocation.value) return;

  isLoading.value = resetPage; // 초기 로드일 때만 전체 로딩 표시
  if (!resetPage) {
    isLoadingMore.value = true; // 추가 로드일 때는 무한 스크롤 로딩만 표시
  }
  error.value = null;

  // 새로운 검색인 경우 페이지 초기화
  if (resetPage) {
    searchParams.page = 0;
    stores.value = [];
  }

  try {
    const params = {
      ...searchParams,
      latitude: currentLocation.value.latitude,
      longitude: currentLocation.value.longitude
    };

    // 성능 측정 시작
    const startTime = performance.now();

    // 최적화된 API 호출
    const response = await storeApi.searchStoresWithLocation(params);
    
    // 성능 측정 종료
    const endTime = performance.now();
    console.log(`API 호출 시간: ${Math.round(endTime - startTime)}ms`);
    
    if (response.data) {
      const newStores = response.data.content || [];
      
      if (resetPage) {
        // 새로운 검색인 경우 기존 목록 교체
        stores.value = newStores;
      } else {
        // 추가 로드인 경우 기존 목록에 추가
        stores.value = [...stores.value, ...newStores];
      }

      // 페이지네이션 정보 업데이트
      pagination.page = response.data.page || 0;
      pagination.size = response.data.size || 20;
      pagination.totalElements = response.data.totalElements || 0;
      pagination.totalPages = response.data.totalPages || 0;
      pagination.hasNext = !response.data.last;
      pagination.isLast = response.data.last || false;
    }
  } catch (err) {
    console.error('Failed to search stores:', err);
    error.value = err;
    
    // 사용자에게 표시할 오류 메시지 설정
    if (err.response) {
      // 서버에서 응답이 온 경우
      if (err.response.status === 404) {
        error.value = { message: '음식점을 찾을 수 없습니다.' };
      } else if (err.response.status === 500) {
        error.value = { message: '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.' };
      } else {
        error.value = { message: `오류가 발생했습니다: ${err.response.data?.message || err.message}` };
      }
    } else if (err.request) {
      // 요청은 보냈지만 응답이 없는 경우 (네트워크 오류)
      error.value = { message: '서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.' };
    } else {
      // 요청 설정 중 오류가 발생한 경우
      error.value = { message: '요청을 처리하는 중 오류가 발생했습니다.' };
    }
    
    if (resetPage) {
      stores.value = [];
    }
  } finally {
    isLoading.value = false;
    isLoadingMore.value = false;
  }
};

// 무한 스크롤로 추가 데이터 로드
const loadMoreStores = async (event) => {
  if (isLoadingMore.value || !hasMoreData.value) {
    event.target.complete();
    return;
  }

  try {
    // 다음 페이지로 이동
    searchParams.page += 1;
    
    // 추가 데이터 로드 (resetPage = false)
    await searchStores(false);
    
  } catch (err) {
    console.error('Failed to load more stores:', err);
    // 에러 발생 시 페이지 롤백
    searchParams.page = Math.max(0, searchParams.page - 1);
  } finally {
    event.target.complete();
    
    // 더 이상 데이터가 없으면 무한 스크롤 비활성화
    if (!hasMoreData.value) {
      event.target.disabled = true;
    }
  }
};

// 디바운스된 검색
const debouncedSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout);
  }
  
  searchTimeout = setTimeout(() => {
    // 새로운 검색 시 첫 페이지로 (resetPage = true)
    searchStores(true);
  }, 500);
};

// 이벤트 핸들러
const handleLocationChanged = (location) => {
  console.log('Location changed:', location);
  searchStores(true);
};

const handlePermissionChanged = (status) => {
  console.log('Permission changed:', status);
};

const goToStoreDetail = (storeId) => {
  router.push(`/stores/${storeId}`);
};

const expandSearchRadius = () => {
  if (searchParams.radiusKm < 20) {
    searchParams.radiusKm = Math.min(searchParams.radiusKm * 2, 20);
    searchStores(true);
  }
};

// 유틸리티 함수들
const formatDistance = (distance) => {
  if (distance < 1) {
    return `${Math.round(distance * 1000)}m`;
  } else if (distance < 10) {
    return `${distance.toFixed(1)}km`;
  } else {
    return `${Math.round(distance)}km`;
  }
};

const formatPrice = (price) => {
  if (!price || price === 0) return '무료';
  return `${price.toLocaleString()}원`;
};

// 위치 새로고침 핸들러
const handleRefreshLocation = async () => {
  isLoading.value = true;
  try {
    await refreshLocation(true); // 강제 새로고침
    if (currentLocation.value) {
      // 위치 정보가 업데이트되면 검색 결과도 새로고침
      await searchStores(true);
    }
  } catch (err) {
    console.error('Failed to refresh location:', err);
    error.value = { message: '위치 정보를 업데이트하는데 실패했습니다.' };
  } finally {
    isLoading.value = false;
  }
};

// 초기 로드
onMounted(() => {
  // 위치가 이미 있으면 즉시 검색
  if (currentLocation.value) {
    searchStores(true);
  }
});
</script>

<style scoped>
/* 요기요 스타일 색상 변수 */
.store-list-page {
  --yogiyo-primary: #ff1744;
  --yogiyo-secondary: #e50032;
  --yogiyo-background: #ffffff;
  --yogiyo-light-gray: #f8f9fa;
  --yogiyo-border-gray: #e9ecef;
  --yogiyo-text-primary: #333333;
  --yogiyo-text-secondary: #666666;
  --yogiyo-text-placeholder: #999999;
}

/* 헤더 스타일 */
.store-list-page ion-header ion-toolbar {
  --background: linear-gradient(135deg, #ff1744 0%, #e50032 100%);
  --color: white;
}

.store-list-page ion-header ion-title {
  font-weight: 600;
  font-size: 18px;
  color: white;
}

.store-list-page ion-header ion-button {
  --color: white;
}

/* 위치 헤더 */
.location-header {
  background: white;
  border-bottom: 1px solid var(--yogiyo-border-gray);
  padding: 16px;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  cursor: pointer;
}

.location-icon {
  color: var(--yogiyo-primary);
  font-size: 20px;
}

.location-text {
  flex: 1;
}

.current-location {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--yogiyo-text-primary);
}

.location-subtitle {
  font-size: 13px;
  color: var(--yogiyo-text-secondary);
}

.location-arrow {
  color: var(--yogiyo-text-secondary);
}

/* 필터 바 */
.filter-bar {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-chip {
  flex-shrink: 0;
  font-size: 13px;
  height: 32px;
}

.filter-chip[color="primary"] {
  --background: var(--yogiyo-primary);
  --color: white;
}

.filter-chip[color="medium"] {
  --background: var(--yogiyo-light-gray);
  --color: var(--yogiyo-text-secondary);
}

/* 정렬 헤더 */
.sort-header {
  background: white;
  border-bottom: 1px solid var(--yogiyo-border-gray);
  padding: 0 16px;
}

.sort-tabs {
  display: flex;
  gap: 24px;
}

.sort-tab {
  background: none;
  border: none;
  padding: 16px 0;
  font-size: 14px;
  color: var(--yogiyo-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
}

.sort-tab.active {
  color: var(--yogiyo-primary);
  border-bottom-color: var(--yogiyo-primary);
  font-weight: 600;
}

/* 가게 목록 컨테이너 */
.store-list-container {
  background: var(--yogiyo-light-gray);
  min-height: calc(100vh - 200px);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  text-align: center;
}

.loading-container p {
  margin-top: 16px;
  color: var(--yogiyo-text-secondary);
}

/* 빈 상태 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  text-align: center;
}

.empty-icon {
  font-size: 80px;
  color: var(--yogiyo-text-placeholder);
  margin-bottom: 24px;
  opacity: 0.7;
}

.empty-state h2 {
  margin: 0 0 12px 0;
  color: var(--yogiyo-text-primary);
  font-size: 20px;
  font-weight: 600;
}

.empty-state p {
  margin: 0;
  color: var(--yogiyo-text-secondary);
  font-size: 16px;
}

/* 가게 목록 */
.store-list {
  padding: 16px;
}

.store-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.store-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

/* 가게 이미지 */
.store-image-container {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.store-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.store-image-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--yogiyo-light-gray) 0%, var(--yogiyo-border-gray) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.store-image-placeholder ion-icon {
  font-size: 48px;
  color: var(--yogiyo-text-placeholder);
}

/* 찜 버튼 */
.favorite-button {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  --color: var(--yogiyo-text-secondary);
}

.favorite-active {
  color: var(--yogiyo-primary) !important;
}

/* 배지 */
.badges {
  position: absolute;
  bottom: 12px;
  left: 12px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.badge-discount {
  background: var(--yogiyo-primary);
}

.badge-free-delivery {
  background: #4caf50;
}

.badge-new {
  background: #ff9800;
}

/* 가게 정보 */
.store-info {
  padding: 16px;
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.store-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--yogiyo-text-primary);
  line-height: 1.3;
}

.store-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 12px;
}

.star-icon {
  color: #ffc107;
  font-size: 14px;
}

.rating-score {
  font-size: 14px;
  font-weight: 600;
  color: var(--yogiyo-text-primary);
}

.review-count {
  font-size: 13px;
  color: var(--yogiyo-text-secondary);
}

.store-description {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: var(--yogiyo-text-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 가게 메타 정보 */
.store-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.delivery-info {
  display: flex;
  gap: 16px;
}

.delivery-time,
.delivery-fee {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--yogiyo-text-secondary);
}

.delivery-time ion-icon,
.delivery-fee ion-icon {
  font-size: 12px;
}

.order-info {
  display: flex;
  gap: 16px;
}

.minimum-order {
  font-size: 13px;
  color: var(--yogiyo-text-secondary);
}

/* 쿠폰 정보 */
.coupon-info {
  border-top: 1px solid var(--yogiyo-border-gray);
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.coupon-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.coupon-icon {
  color: #2196f3;
  font-size: 14px;
}

.coupon-text {
  font-size: 13px;
  color: #2196f3;
  font-weight: 500;
}

/* 모달 스타일 */
.search-content,
.location-content {
  padding: 16px;
}

.search-content h4,
.location-content h4 {
  margin: 24px 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--yogiyo-text-primary);
}

.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.search-tag {
  font-size: 14px;
  height: 32px;
}

.search-tag.popular {
  --background: var(--yogiyo-light-gray);
  --color: var(--yogiyo-text-primary);
}

.location-button {
  --background: var(--yogiyo-primary);
  --background-activated: var(--yogiyo-secondary);
  --color: white;
  --border-radius: 8px;
  height: 48px;
  margin: 16px 0;
}

.location-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.location-item:hover {
  background: var(--yogiyo-light-gray);
}

.location-item-icon {
  color: var(--yogiyo-text-secondary);
  font-size: 16px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .store-card {
    margin-bottom: 12px;
  }
  
  .store-image-container {
    height: 160px;
  }
  
 .store-info {
    padding: 12px;
  }
  
  .store-name {
    font-size: 16px;
  }
  
  .filter-bar {
    gap: 6px;
  }
  
  .sort-tabs {
    gap: 16px;
  }
}

/* 애니메이션 효과 */
.store-card {
  animation: fadeInUp 0.3s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 로딩 스켈레톤 */
.loading-skeleton {
  background: linear-gradient(90deg, var(--yogiyo-light-gray) 25%, var(--yogiyo-border-gray) 50%, var(--yogiyo-light-gray) 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style> 