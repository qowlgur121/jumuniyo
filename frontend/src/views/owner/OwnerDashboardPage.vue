<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>사장님 대시보드</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="logout">
            <ion-icon :icon="logOutOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="owner-dashboard-content">
      <div class="responsive-container">
        <!-- 환영 헤더 -->
        <div class="welcome-header">
          <div class="owner-info">
            <div class="logo">
              <span class="logo-text">주문이요</span>
              <span class="owner-badge">사장님</span>
            </div>
            <h1 class="welcome-title">안녕하세요, {{ user?.nickname }}님!</h1>
            <p class="welcome-subtitle">오늘도 맛있는 음식으로 고객들을 만족시켜보세요 🍽️</p>
          </div>
        </div>

        <!-- 가게가 없을 때 빈 상태 -->
        <div v-if="stores.length === 0" class="empty-stores-state">
          <div class="empty-icon">🏪</div>
          <h2>등록된 가게가 없습니다</h2>
          <p>먼저 가게를 등록하여 사업을 시작해보세요!</p>
          <ion-button 
            expand="block" 
            @click="registerStore"
            class="register-store-button"
          >
            가게 등록하기
          </ion-button>
        </div>

        <!-- 가게는 있지만 선택되지 않았을 때 -->
        <div v-if="stores.length > 0 && !selectedStoreId" class="select-store-state">
          <div class="empty-icon">📊</div>
          <h2>가게를 선택해주세요</h2>
          <p>위에서 관리할 가게를 선택하면 대시보드가 표시됩니다.</p>
        </div>

        <!-- 가게 선택 섹션 -->
        <div class="store-selection-section" v-if="stores.length > 0">
          <div class="store-selector">
            <h2>가게 선택</h2>
            <div class="selector-row">
              <ion-select
                v-model="selectedStoreId"
                placeholder="관리할 가게를 선택하세요"
                interface="action-sheet"
                @ionChange="onStoreChange"
              >
                <ion-select-option
                  v-for="store in stores"
                  :key="store.id"
                  :value="store.id"
                >
                  {{ store.name }}
                </ion-select-option>
              </ion-select>
              <ion-button 
                fill="outline" 
                @click="registerStore"
                class="add-store-button"
              >
                <ion-icon :icon="addOutline" slot="start"></ion-icon>
                가게 추가
              </ion-button>
            </div>
          </div>
        </div>

                <!-- 선택된 가게 정보 -->
        <div v-if="selectedStore" class="selected-store-info">
          <div class="store-header">
            <div class="store-main-info">
              <h2>{{ selectedStore.name }} 대시보드</h2>
              <p>{{ selectedStore.address }}</p>
              <div class="store-meta" v-if="getCategoryName(selectedStore.category) !== '카테고리 없음' || getStoreStatusText(selectedStore.status) !== '상태불명'">
                <span class="category-tag" v-if="getCategoryName(selectedStore.category) !== '카테고리 없음'">{{ getCategoryName(selectedStore.category) }}</span>
                <span class="status-indicator" v-if="getStoreStatusText(selectedStore.status) !== '상태불명'" :class="getStatusClass(selectedStore.status)">
                  {{ getStoreStatusText(selectedStore.status) }}
                </span>
              </div>
            </div>
            <div class="store-actions">
              <ion-button 
                fill="outline" 
                size="small"
                @click="editStoreInfo"
                class="edit-store-button"
              >
                <ion-icon :icon="pencilOutline" slot="start"></ion-icon>
                가게 정보 수정
              </ion-button>
              <ion-button 
                fill="clear" 
                size="small"
                color="danger"
                @click="confirmDeleteStore"
                class="delete-store-button"
              >
                <ion-icon :icon="trashOutline" slot="start"></ion-icon>
                가게 삭제
              </ion-button>
            </div>
          </div>
          
          <!-- 영업 상태 토글 섹션 -->
          <div class="business-status-section">
            <div class="status-toggle-container">
              <div class="status-info">
                <div class="status-icon-wrapper" 
                     :class="selectedStore.isActive ? 'icon-wrapper-active' : 'icon-wrapper-inactive'">
                  <ion-icon :icon="selectedStore.isActive ? checkmarkCircle : closeCircle" 
                           :class="selectedStore.isActive ? 'status-icon-active' : 'status-icon-inactive'"></ion-icon>
                </div>
                <div class="status-text">
                  <h3>{{ selectedStore.isActive ? '영업중' : '휴업중' }}</h3>
                  <p>{{ selectedStore.isActive ? '고객이 주문할 수 있습니다' : '주문 접수가 중단됩니다' }}</p>
                </div>
              </div>
              <ion-toggle
                :checked="selectedStore.isActive"
                @ionChange="toggleStoreStatus"
                :disabled="isTogglingStatus"
                class="status-toggle"
                :color="selectedStore.isActive ? 'success' : 'medium'"
              ></ion-toggle>
            </div>
          </div>
        </div>

        <!-- 통계 카드들 (선택된 가게 기준) -->
        <div class="stats-section" v-if="selectedStore">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-info">
                <h3>{{ selectedStore.name }} 오늘 주문</h3>
                <p class="stat-number">{{ selectedStoreStats.todayOrders }}</p>
                <span class="stat-change positive">+12% 어제 대비</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">💰</div>
              <div class="stat-info">
                <h3>{{ selectedStore.name }} 오늘 매출</h3>
                <p class="stat-number">{{ formatCurrency(selectedStoreStats.todaySales) }}</p>
                <span class="stat-change positive">+8% 어제 대비</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">⭐</div>
              <div class="stat-info">
                <h3>{{ selectedStore.name }} 평점</h3>
                <p class="stat-number">{{ selectedStoreStats.averageRating }}</p>
                <span class="stat-change neutral">리뷰 {{ selectedStoreStats.reviewCount }}개</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">🚚</div>
              <div class="stat-info">
                <h3>배달 중인 주문</h3>
                <p class="stat-number">{{ selectedStoreStats.deliveringOrders }}</p>
                <span class="stat-change neutral">실시간</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 빠른 액션 -->
        <div class="quick-actions-section">
          <h2 class="section-title">빠른 작업</h2>
          <div class="action-grid">
            <div class="action-card" @click="goToOrders">
              <div class="action-icon">📋</div>
              <h3>주문 관리</h3>
              <p>새로운 주문을 확인하고 처리하세요</p>
              <div class="notification-badge" v-if="newOrdersCount > 0">{{ newOrdersCount }}</div>
            </div>

            <div 
              class="action-card" 
              :class="{ 'disabled': !selectedStoreId }"
              @click="editStoreInfo"
            >
              <div class="action-icon">🏪</div>
              <h3>가게 정보</h3>
              <p v-if="selectedStoreId">{{ selectedStore?.name }} 정보를 수정하세요</p>
              <p v-else>가게를 선택한 후 정보를 수정하세요</p>
            </div>

            <div 
              class="action-card" 
              :class="{ 'disabled': !selectedStoreId }"
              @click="goToMenuManagement"
            >
              <div class="action-icon">🍽️</div>
              <h3>메뉴 관리</h3>
              <p v-if="selectedStoreId">{{ selectedStore?.name }} 메뉴를 관리하세요</p>
              <p v-else>가게를 선택한 후 메뉴를 관리하세요</p>
            </div>

            <div class="action-card" @click="goToAnalytics">
              <div class="action-icon">📈</div>
              <h3>매출 분석</h3>
              <p>매출 통계와 트렌드를 확인하세요</p>
            </div>

            <div class="action-card" @click="goToProfile">
              <div class="action-icon">👤</div>
              <h3>프로필 설정</h3>
              <p>계정 정보와 설정을 변경하세요</p>
            </div>
          </div>
        </div>

        <!-- 최근 주문 -->
        <div class="recent-orders-section">
          <div class="section-header">
            <h2 class="section-title">최근 주문</h2>
            <ion-button fill="clear" size="small" @click="goToOrders">
              전체 보기
              <ion-icon :icon="chevronForwardOutline" slot="end"></ion-icon>
            </ion-button>
          </div>

          <div class="orders-list">
            <div 
              v-for="order in recentOrders" 
              :key="order.id" 
              class="order-item"
              @click="viewOrder(order.id)"
            >
              <div class="order-info">
                <h3>{{ order.customerName }}</h3>
                <p>{{ order.items.join(', ') }}</p>
                <span class="order-time">{{ formatTime(order.orderTime) }}</span>
              </div>
              <div class="order-status">
                <span :class="['status-badge', order.status]">{{ getStatusText(order.status) }}</span>
                <p class="order-amount">{{ formatCurrency(order.amount) }}</p>
              </div>
            </div>

            <div v-if="recentOrders.length === 0" class="empty-state">
              <p>최근 주문이 없습니다</p>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonButton,
  IonIcon,
  IonSelect,
  IonSelectOption,
  IonToggle,
  toastController,
  alertController
} from '@ionic/vue';
import {
  logOutOutline,
  chevronForwardOutline,
  addOutline,
  pencilOutline,
  checkmarkCircle,
  closeCircle,
  trashOutline
} from 'ionicons/icons';
// @ts-ignore
import apiClient from '@/services/api';

const router = useRouter();
const authStore = useAuthStore();

// 사용자 정보
const user = computed(() => authStore.user);

// 가게 관련 상태
const stores = ref([]);
const selectedStoreId = ref(null);
const categories = ref([]);
const isTogglingStatus = ref(false);
const selectedStore = computed(() => {
  return stores.value.find(store => store.id === selectedStoreId.value);
});

// 선택된 가게의 통계 데이터
const selectedStoreStats = computed(() => {
  if (!selectedStoreId.value) {
    return {
      todayOrders: 0,
      todaySales: 0,
      averageRating: 0,
      reviewCount: 0,
      deliveringOrders: 0
    };
  }
  
  // 실제로는 API에서 가져올 데이터 (가게별로)
  return {
    todayOrders: 15,
    todaySales: 320000,
    averageRating: 4.7,
    reviewCount: 89,
    deliveringOrders: 3
  };
});

// 기존 전체 통계 데이터 (참고용)
const todayOrders = ref(24);
const todaySales = ref(480000);
const averageRating = ref(4.8);
const reviewCount = ref(126);
const storeCount = ref(1);
const newOrdersCount = ref(3);

// 최근 주문 데이터 (실제로는 API에서 가져올 데이터)
const recentOrders = ref([
  {
    id: 1,
    customerName: '김**님',
    items: ['치킨 세트', '콜라'],
    orderTime: new Date(Date.now() - 10 * 60 * 1000),
    status: 'pending',
    amount: 28000
  },
  {
    id: 2,
    customerName: '이**님',
    items: ['피자 라지', '사이다'],
    orderTime: new Date(Date.now() - 25 * 60 * 1000),
    status: 'cooking',
    amount: 35000
  },
  {
    id: 3,
    customerName: '박**님',
    items: ['족발 대', '막국수'],
    orderTime: new Date(Date.now() - 45 * 60 * 1000),
    status: 'delivered',
    amount: 55000
  }
]);

// 통화 포맷팅
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount);
};

// 시간 포맷팅
const formatTime = (date) => {
  const now = new Date();
  const diff = Math.floor((now - date) / (1000 * 60)); // 분 단위
  
  if (diff < 1) return '방금 전';
  if (diff < 60) return `${diff}분 전`;
  if (diff < 1440) return `${Math.floor(diff / 60)}시간 전`;
  return date.toLocaleDateString('ko-KR');
};

// 상태 텍스트
const getStatusText = (status) => {
  const statusMap = {
    pending: '주문 접수',
    cooking: '조리 중',
    delivery: '배달 중',
    delivered: '배달 완료',
    cancelled: '취소됨'
  };
  return statusMap[status] || status;
};

// 네비게이션 함수들
const goToOrders = () => {
  router.push('/owner/orders');
};

const goToMenuManagement = () => {
  if (!selectedStoreId.value) {
    const toast = toastController.create({
      message: '가게를 먼저 선택해주세요.',
      duration: 2000,
      color: 'warning'
    });
    toast.then(t => t.present());
    return;
  }
  router.push(`/store/${selectedStoreId.value}/menu-management`);
};

const goToAnalytics = () => {
  router.push('/owner/analytics');
};

const goToProfile = () => {
  router.push('/owner/profile');
};

const viewOrder = (orderId) => {
  router.push(`/owner/orders/${orderId}`);
};

// 로그아웃
const logout = async () => {
  await authStore.logout();
  const toast = await toastController.create({
    message: '로그아웃되었습니다',
    duration: 2000,
    color: 'success',
    position: 'top'
  });
  await toast.present();
  router.push('/owner/login');
};

// 컴포넌트 마운트 시 실행
onMounted(async () => {
  await Promise.all([
    loadStores(),
    loadCategories()
  ]);
  // 가게가 하나만 있으면 자동 선택
  if (stores.value.length === 1) {
    selectedStoreId.value = stores.value[0].id;
  }
});

// 가게 목록 로드
const loadStores = async () => {
  try {
    const response = await apiClient.get('/stores/my');
    stores.value = response.data.content || [];
  } catch (error) {
    console.error('가게 목록 로드 실패:', error);
    showToast('가게 정보를 불러오는데 실패했습니다.', 'warning');
  }
};

// 가게 선택 변경 시
const onStoreChange = () => {
  // 선택된 가게가 변경되면 해당 가게의 데이터를 로드
  if (selectedStoreId.value) {
    loadStoreStats();
  }
};

// 선택된 가게의 통계 데이터 로드
const loadStoreStats = async () => {
  if (!selectedStoreId.value) return;
  
  try {
    // 실제로는 API에서 가게별 통계를 가져올 예정
    // const response = await apiClient.get(`/stores/${selectedStoreId.value}/stats`);
    // 현재는 더미 데이터 사용
  } catch (error) {
    console.error('가게 통계 로드 실패:', error);
    showToast('가게 통계를 불러오는데 실패했습니다.', 'warning');
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

// 가게 등록 함수
const registerStore = () => {
  router.push('/store/register');
};

// 가게 정보 수정 함수
const editStoreInfo = () => {
  if (selectedStoreId.value) {
    router.push(`/store/edit/${selectedStoreId.value}`);
  } else {
    showToast('수정할 가게를 먼저 선택해주세요.', 'warning');
  }
};

// 카테고리 이름 가져오기
const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.id === categoryId);
  return category ? category.name : '카테고리 없음';
};

// 상태 클래스 가져오기
const getStatusClass = (status) => {
  switch (status) {
    case 'ACTIVE': return 'status-active';
    case 'INACTIVE': return 'status-inactive';
    case 'PENDING': return 'status-pending';
    default: return 'status-unknown';
  }
};

// 상태 텍스트 가져오기 (가게 상태용)
const getStoreStatusText = (status) => {
  switch (status) {
    case 'ACTIVE': return '운영중';
    case 'INACTIVE': return '휴업중';
    case 'PENDING': return '승인대기';
    default: return '상태불명';
  }
};

// 영업 상태 토글
const toggleStoreStatus = async () => {
  if (!selectedStore.value) return;
  
  try {
    isTogglingStatus.value = true;
    const response = await apiClient.post(`/stores/${selectedStore.value.id}/toggle-status`);
    
    if (response.data.isActive !== undefined) {
      selectedStore.value.isActive = response.data.isActive;
      showToast(response.data.message || '영업 상태가 성공적으로 변경되었습니다.');
    } else {
      showToast('영업 상태 변경에 실패했습니다. 다시 시도해주세요.', 'warning');
    }
  } catch (error) {
    console.error('영업 상태 변경 실패:', error);
    showToast('영업 상태 변경에 실패했습니다. 다시 시도해주세요.', 'danger');
  } finally {
    isTogglingStatus.value = false;
  }
};

// 가게 삭제 확인
const confirmDeleteStore = async () => {
  if (!selectedStore.value) return;
  
  const alert = await alertController.create({
    header: '가게 삭제',
    message: `정말로 "${selectedStore.value.name}" 가게를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`,
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '삭제',
        role: 'destructive',
        handler: () => {
          deleteStore();
        }
      }
    ]
  });
  
  await alert.present();
};

// 가게 삭제
const deleteStore = async () => {
  if (!selectedStore.value) return;
  
  try {
    await apiClient.delete(`/stores/${selectedStore.value.id}`);
    showToast('가게가 성공적으로 삭제되었습니다.');
    
    // 가게 목록에서 제거
    stores.value = stores.value.filter(store => store.id !== selectedStore.value.id);
    selectedStoreId.value = null;
    
    // 가게 목록 새로고침
    await loadStores();
  } catch (error) {
    console.error('가게 삭제 실패:', error);
    
    let errorMessage = '가게 삭제에 실패했습니다.';
    
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    } else if (error.response?.data?.error) {
      errorMessage = error.response.data.error;
    } else if (error.response?.status === 403) {
      errorMessage = '삭제 권한이 없습니다.';
    } else if (error.response?.status === 404) {
      errorMessage = '가게를 찾을 수 없습니다.';
    } else if (error.response?.status === 500) {
      errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
    
    showToast(errorMessage, 'danger');
  }
};

// 카테고리 목록 로드
const loadCategories = async () => {
  try {
    const response = await apiClient.get('/categories');
    categories.value = response.data || [];
  } catch (error) {
    console.error('카테고리 로드 실패:', error);
  }
};
</script>

<style scoped>
.owner-dashboard-content {
  --background: #f5f5f5;
}

/* 전체 컨테이너를 위한 최대 폭 설정 */
.responsive-container {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.welcome-header {
  background: linear-gradient(135deg, #ff1744 0%, #e91e63 100%);
  color: white;
  padding: 2rem 1rem;
  text-align: center;
}

.owner-info {
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
  font-size: 1.5rem;
  font-weight: 900;
  color: white;
}

.owner-badge {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.welcome-title {
  margin: 0 0 0.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
}

.welcome-subtitle {
  margin: 0;
  font-size: 1rem;
  opacity: 0.9;
  color: white;
}

.stats-section {
  padding: 1.5rem;
  max-width: 100%;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  max-width: 100%;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1rem;
  min-height: 100px;
  box-sizing: border-box;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2rem;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 0.75rem;
  border-radius: 12px;
  width: 3.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  color: var(--ion-color-medium);
  font-weight: 500;
}

.stat-number {
  margin: 0 0 0.25rem 0;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--ion-color-dark);
}

.stat-change {
  font-size: 0.8rem;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
}

.stat-change.positive {
  background: #e8f5e8;
  color: #2dd36f;
}

.stat-change.negative {
  background: #fdeaea;
  color: #eb445a;
}

.stat-change.neutral {
  background: var(--ion-color-light);
  color: var(--ion-color-medium);
}

.quick-actions-section,
.recent-orders-section {
  padding: 0 1.5rem 1.5rem;
}

.section-title {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--ion-color-dark);
  text-align: center;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
}

.action-card {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 0.75rem;
}

.action-card h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.action-card p {
  margin: 0;
  font-size: 0.8rem;
  color: var(--ion-color-medium);
  line-height: 1.4;
}

.notification-badge {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  background: #ff1744;
  color: white;
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  min-width: 1.5rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(255, 23, 68, 0.3);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.orders-list {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--ion-color-light);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item:hover {
  background: var(--ion-color-light);
}

.order-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.order-info p {
  margin: 0 0 0.25rem 0;
  font-size: 0.9rem;
  color: var(--ion-color-medium);
}

.order-time {
  font-size: 0.8rem;
  color: var(--ion-color-medium);
}

.order-status {
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 0.3rem 0.75rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.cooking {
  background: #d1ecf1;
  color: #0c5460;
}

.status-badge.delivery {
  background: #cce5ff;
  color: #004085;
}

.status-badge.delivered {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.order-amount {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.empty-state {
  padding: 2rem;
  text-align: center;
  color: var(--ion-color-medium);
}

/* 반응형 디자인 개선 */
@media (min-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .action-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .welcome-header {
    padding: 1.5rem 1rem;
  }
  
  .welcome-title {
    font-size: 1.3rem;
  }
  
  .welcome-subtitle {
    font-size: 0.9rem;
  }
  
  .stats-section {
    padding: 1rem;
  }
  
  .quick-actions-section,
  .recent-orders-section {
    padding: 0 1rem 1.5rem;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .action-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
  
  .stat-card {
    padding: 1rem;
    flex-direction: row;
    text-align: left;
  }
  
  .action-card {
    padding: 1rem;
  }
  
  .order-item {
    padding: 1rem;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }
  
  .order-status {
    text-align: left;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

/* 가게 선택 섹션 */
.store-selection-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.store-selector h2 {
  font-size: 1.3rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  text-align: center;
}

.selector-row {
  display: flex;
  gap: 16px;
  align-items: center;
}

.store-selector ion-select {
  --background: #f8f9fa;
  --border-radius: 12px;
  --border-color: #e9ecef;
  --padding-start: 16px;
  --padding-end: 16px;
  font-weight: 500;
  flex: 1;
}

.add-store-button {
  --border-radius: 12px;
  --padding-start: 16px;
  --padding-end: 16px;
  flex-shrink: 0;
}

/* 선택된 가게 정보 */
.selected-store-info {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.store-main-info h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.store-main-info p {
  color: #666;
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

.store-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.edit-store-button {
  --border-radius: 12px;
}

.delete-store-button {
  --color: #dc3545;
  --border-radius: 12px;
}

/* 영업 상태 토글 스타일 */
.business-status-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.status-toggle-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  transition: background-color 0.3s ease;
}

.icon-wrapper-active {
  background: rgba(40, 167, 69, 0.15);
}

.icon-wrapper-inactive {
  background: rgba(220, 53, 69, 0.15);
}

.status-icon-active {
  color: #28a745;
  font-size: 1.8rem;
}

.status-icon-inactive {
  color: #dc3545;
  font-size: 1.8rem;
}

.status-text {
  display: flex;
  flex-direction: column;
}

.status-text h3 {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.status-text p {
  font-size: 1rem;
  color: #6c757d;
  margin: 0;
}

.status-toggle {
  --background-checked: #28a745;
  --background-unchecked: #dc3545;
  --handle-background-checked: white;
  --handle-background-unchecked: white;
  --handle-border-radius: 50%;
  --handle-border-width: 0;
  --handle-height: 28px;
  --handle-width: 28px;
  --handle-box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  --handle-transform: none;
  --handle-transition: transform 0.3s ease;
  --border-radius: 22px;
  --height: 36px;
  --width: 64px;
}

/* 빈 상태 공통 스타일 */
.empty-stores-state,
.select-store-state {
  padding: 2rem;
  text-align: center;
  color: var(--ion-color-medium);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.register-store-button {
  --background: var(--ion-color-primary);
  --background-activated: var(--ion-color-primary-shade);
  --background-hover: var(--ion-color-primary-tint);
  --color: white;
  --color-activated: white;
  --color-hover: white;
  --border-radius: 12px;
  --border-width: 0;
  --padding-top: 12px;
  --padding-bottom: 12px;
  --padding-start: 24px;
  --padding-end: 24px;
  font-weight: 600;
}

/* 액션 카드 비활성화 스타일 */
.action-card.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

.action-card.disabled:hover {
  transform: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
</style> 