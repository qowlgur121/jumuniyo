<template>
  <ion-page>
    <ion-header class="responsive-header">
      <ion-toolbar class="responsive-toolbar">
        <ion-title class="responsive-page-title">주문내역</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="order-history-container responsive-container">
        <!-- 로그인 유도 섹션 (미로그인 시) -->
        <div class="login-prompt spacing-xl" v-if="!isAuthenticated">
          <div class="prompt-card">
            <div class="empty-icon">
              <ion-icon :icon="receiptOutline" class="large-icon"></ion-icon>
            </div>
            <h3 class="prompt-title responsive-subtitle">주문내역을 확인하려면 로그인하세요</h3>
            <p class="prompt-desc responsive-small">로그인하고 주문 내역을 확인해보세요.</p>
            <ion-button 
              expand="block" 
              class="responsive-button login-prompt-button"
              @click="goToLogin"
            >
              로그인하기
            </ion-button>
          </div>
        </div>

        <!-- 주문내역 목록 (로그인 시) -->
        <div v-else>
          <!-- 로딩 상태 -->
          <div class="loading-state spacing-xl" v-if="isLoading">
            <div class="loading-card">
              <ion-spinner name="bubbles" color="primary"></ion-spinner>
              <p class="loading-text responsive-small">주문내역을 불러오는 중...</p>
            </div>
          </div>

          <!-- 에러 상태 -->
          <div class="error-state spacing-xl" v-else-if="error">
            <div class="error-card">
              <div class="empty-icon">
                <ion-icon :icon="alertCircleOutline" class="large-icon error-icon"></ion-icon>
              </div>
              <h3 class="error-title responsive-subtitle">주문내역을 불러올 수 없어요</h3>
              <p class="error-desc responsive-small">{{ error }}</p>
              <ion-button 
                expand="block" 
                class="responsive-button retry-button"
                @click="loadOrders"
              >
                다시 시도
              </ion-button>
            </div>
          </div>

          <!-- 빈 상태 -->
          <div class="empty-state spacing-xl" v-else-if="orders.length === 0">
            <div class="empty-icon">
              <ion-icon :icon="receiptOutline" class="large-icon"></ion-icon>
            </div>
            <h3 class="empty-title responsive-subtitle">주문내역이 없어요</h3>
            <p class="empty-desc responsive-small">맛있는 음식을 주문해보세요!</p>
            <ion-button 
              expand="block" 
              class="responsive-button order-button"
              @click="goToHome"
            >
              주문하러 가기
            </ion-button>
          </div>

          <!-- 주문내역 리스트 -->
          <div class="order-list" v-else>
            <div class="order-item" v-for="order in orders" :key="order.id" @click="goToOrderDetail(order.id)">
              <div class="order-header">
                <div class="order-date responsive-small">{{ formatDate(order.createdAt) }}</div>
                <div class="order-status" :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </div>
              </div>
              
              <div class="order-content">
                <div class="restaurant-info">
                  <h4 class="restaurant-name responsive-body">{{ order.storeName }}</h4>
                  <p class="order-items responsive-small">{{ formatOrderItems(order.orderItems) }}</p>
                  <p class="order-number responsive-small">주문번호: {{ order.orderNumber }}</p>
                </div>
                
                <div class="order-actions">
                  <div class="order-price responsive-body">{{ formatPrice(order.totalPrice) }}원</div>
                  <ion-button 
                    fill="outline" 
                    size="small" 
                    class="cancel-button"
                    @click.stop="cancelOrder(order)"
                    v-if="canCancelOrder(order.status)"
                  >
                    주문취소
                  </ion-button>
                  <ion-button 
                    fill="outline" 
                    size="small" 
                    class="reorder-button"
                    @click.stop="reorder(order)"
                    v-if="order.status === 'DELIVERED'"
                  >
                    재주문
                  </ion-button>
                </div>
              </div>
            </div>

            <!-- 더 보기 버튼 (페이징) -->
            <div class="load-more-section" v-if="hasMoreData">
              <ion-button 
                expand="block" 
                fill="outline"
                class="load-more-button"
                @click="loadMoreOrders"
                :disabled="isLoadingMore"
              >
                <ion-spinner v-if="isLoadingMore" name="bubbles" class="small-spinner"></ion-spinner>
                <span v-else>더 보기</span>
              </ion-button>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { orderApi } from '@/services/api';
import { 
  IonPage, 
  IonHeader, 
  IonToolbar, 
  IonTitle, 
  IonContent, 
  IonButton,
  IonIcon,
  IonSpinner,
  toastController,
  alertController
} from '@ionic/vue';
import { receiptOutline, alertCircleOutline } from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);

// 상태 관리
const orders = ref([]);
const isLoading = ref(false);
const isLoadingMore = ref(false);
const error = ref(null);
const currentPage = ref(0);
const pageSize = ref(10);
const hasMoreData = ref(true);

// 컴포넌트 마운트 시 주문 내역 로드
onMounted(() => {
  if (isAuthenticated.value) {
    loadOrders();
  }
});

// 주문 내역 로드 함수
const loadOrders = async () => {
  if (!isAuthenticated.value) return;
  
  try {
    isLoading.value = true;
    error.value = null;
    currentPage.value = 0;
    
    const response = await orderApi.getMyOrders(currentPage.value, pageSize.value);
    console.log('주문 내역 응답:', response.data);
    
    if (response.data && response.data.content) {
      orders.value = response.data.content;
      hasMoreData.value = !response.data.last;
    } else {
      orders.value = [];
      hasMoreData.value = false;
    }
  } catch (err) {
    console.error('주문 내역 로드 실패:', err);
    error.value = err.response?.data?.message || '주문 내역을 불러오는데 실패했습니다.';
    orders.value = [];
  } finally {
    isLoading.value = false;
  }
};

// 더 많은 주문 내역 로드 (페이징)
const loadMoreOrders = async () => {
  if (!hasMoreData.value || isLoadingMore.value) return;
  
  try {
    isLoadingMore.value = true;
    const nextPage = currentPage.value + 1;
    
    const response = await orderApi.getMyOrders(nextPage, pageSize.value);
    
    if (response.data && response.data.content) {
      orders.value.push(...response.data.content);
      currentPage.value = nextPage;
      hasMoreData.value = !response.data.last;
    }
  } catch (err) {
    console.error('추가 주문 내역 로드 실패:', err);
    const toast = await toastController.create({
      message: '추가 주문 내역을 불러오는데 실패했습니다.',
      duration: 2000,
      color: 'danger',
      position: 'top',
    });
    await toast.present();
  } finally {
    isLoadingMore.value = false;
  }
};

// 내비게이션 함수들
const goToLogin = () => {
  router.push('/auth/login');
};

const goToHome = () => {
  router.push('/tabs/tab1');
};

const goToOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`);
};

// 포맷팅 함수들
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const now = new Date();
  const diffTime = Math.abs(now - date);
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) {
    return '오늘';
  } else if (diffDays === 1) {
    return '어제';
  } else if (diffDays < 7) {
    return `${diffDays}일 전`;
  } else {
    return `${date.getMonth() + 1}월 ${date.getDate()}일`;
  }
};

const formatPrice = (price) => {
  return price?.toLocaleString() || '0';
};

const formatOrderItems = (orderItems) => {
  if (!orderItems || orderItems.length === 0) return '주문 상품이 없습니다';
  
  const firstItem = orderItems[0];
  const firstItemText = `${firstItem.menuName} ${firstItem.quantity}개`;
  
  if (orderItems.length === 1) {
    return firstItemText;
  } else {
    return `${firstItemText} 외 ${orderItems.length - 1}개`;
  }
};

// 주문 상태 관련 함수들
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '주문대기',
    'CONFIRMED': '주문확인',
    'PREPARING': '조리중',
    'READY_FOR_PICKUP': '픽업대기',
    'IN_DELIVERY': '배달중',
    'DELIVERED': '배달완료',
    'CANCELLED': '주문취소',
    'REJECTED': '주문거절'
  };
  return statusMap[status] || status;
};

const getStatusClass = (status) => {
  const statusClassMap = {
    'PENDING': 'status-pending',
    'CONFIRMED': 'status-confirmed',
    'PREPARING': 'status-preparing',
    'READY_FOR_PICKUP': 'status-ready',
    'IN_DELIVERY': 'status-delivering',
    'DELIVERED': 'status-delivered',
    'CANCELLED': 'status-cancelled',
    'REJECTED': 'status-rejected'
  };
  return statusClassMap[status] || 'status-default';
};

const canCancelOrder = (status) => {
  return ['PENDING', 'CONFIRMED'].includes(status);
};

// 주문 취소 함수
const cancelOrder = async (order) => {
  const alert = await alertController.create({
    header: '주문 취소',
    message: `${order.storeName} 주문을 취소하시겠습니까?`,
    buttons: [
      {
        text: '아니오',
        role: 'cancel'
      },
      {
        text: '예',
        handler: async () => {
          try {
            await orderApi.cancelOrder(order.id, '고객 요청');
            
            const toast = await toastController.create({
              message: '주문이 취소되었습니다.',
              duration: 2000,
              color: 'success',
              position: 'top',
            });
            await toast.present();
            
            // 주문 목록 새로고침
            loadOrders();
          } catch (err) {
            console.error('주문 취소 실패:', err);
            const toast = await toastController.create({
              message: err.response?.data?.message || '주문 취소에 실패했습니다.',
              duration: 2000,
              color: 'danger',
              position: 'top',
            });
            await toast.present();
          }
        }
      }
    ]
  });
  
  await alert.present();
};

// 재주문 함수
const reorder = async (order) => {
  const toast = await toastController.create({
    message: `${order.storeName}에서 재주문 기능은 준비 중입니다.`,
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};
</script>

<style scoped>
.responsive-header {
  --background: var(--background-white);
  --border-color: var(--border-gray);
}

.responsive-toolbar {
  --background: var(--background-white);
  --color: var(--text-primary);
  --border-color: var(--border-gray);
}

.responsive-page-title {
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
  text-align: center;
}

.order-history-container {
  padding-top: 16px;
  padding-bottom: 75px;
}

/* 로그인 유도 및 빈 상태 */
.login-prompt, .empty-state, .loading-state, .error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.prompt-card, .loading-card, .error-card {
  background-color: var(--background-light);
  border-radius: 12px;
  padding: 40px 24px;
  text-align: center;
  max-width: 400px;
  width: 100%;
}

.empty-icon {
  margin-bottom: 24px;
}

.large-icon {
  font-size: 64px;
  color: var(--text-placeholder);
}

.prompt-title, .empty-title {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.prompt-desc, .empty-desc {
  color: var(--text-secondary);
  margin: 0 0 24px 0;
}

.login-prompt-button, .order-button {
  --background: var(--primary-red);
  --background-activated: var(--secondary-red);
  --background-hover: var(--secondary-red);
  --border-radius: 8px;
  --color: white;
  font-weight: 600;
  --box-shadow: none;
}

/* 로딩 상태 */
.loading-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.loading-text {
  color: var(--text-secondary);
  margin: 0;
}

/* 에러 상태 */
.error-card {
  text-align: center;
}

.error-icon {
  color: var(--error-red);
}

.error-title {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.error-desc {
  color: var(--text-secondary);
  margin: 0 0 24px 0;
}

.retry-button {
  --background: var(--primary-red);
  --background-activated: var(--secondary-red);
  --background-hover: var(--secondary-red);
  --border-radius: 8px;
  --color: white;
  font-weight: 600;
  --box-shadow: none;
}

/* 주문번호 스타일 */
.order-number {
  color: var(--text-placeholder);
  font-size: 12px;
  margin-top: 4px;
}

/* 주문 아이템 클릭 효과 */
.order-item {
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-item:active {
  transform: scale(0.98);
  opacity: 0.9;
}

/* 주문 취소 버튼 */
.cancel-button {
  --border-color: var(--error-red);
  --color: var(--error-red);
  --background: transparent;
  font-size: 12px;
  height: 32px;
  min-width: 60px;
  margin-right: 8px;
}

/* 추가 상태 스타일 */
.status-pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-confirmed {
  background-color: #d1ecf1;
  color: #0c5460;
}

.status-ready {
  background-color: #d4edda;
  color: #155724;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.status-default {
  background-color: #e2e3e5;
  color: #383d41;
}

/* 더 보기 버튼 섹션 */
.load-more-section {
  margin-top: 24px;
  padding: 0 16px;
}

.load-more-button {
  --border-color: var(--primary-red);
  --color: var(--primary-red);
  --background: transparent;
  font-weight: 500;
  height: 44px;
}

.small-spinner {
  width: 20px;
  height: 20px;
}

/* 주문내역 리스트 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  background-color: var(--background-white);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.2s ease;
}

.order-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-gray);
}

.order-date {
  color: var(--text-secondary);
}

.order-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.restaurant-info {
  flex: 1;
}

.restaurant-name {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
}

.order-items {
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.4;
}

.order-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.order-price {
  font-weight: 600;
  color: var(--text-primary);
}

.reorder-button {
  --border-color: var(--primary-red);
  --color: var(--primary-red);
  --background: transparent;
  font-size: 12px;
  height: 32px;
  min-width: 60px;
}

/* ===========================================
   태블릿 반응형 스타일 (768px ~ 1023px)
   ========================================== */
@media (min-width: 768px) and (max-width: 1023px) {
  .order-item {
    padding: 20px;
  }
  
  .order-actions {
    flex-direction: row;
    align-items: center;
    gap: 12px;
  }
}

/* ===========================================
   데스크톱 반응형 스타일 (1024px+)
   ========================================== */
@media (min-width: 1024px) {
  .order-list {
    gap: 20px;
  }
  
  .order-item {
    padding: 24px;
  }
  
  .order-actions {
    flex-direction: row;
    align-items: center;
    gap: 16px;
  }
  
  .reorder-button:hover {
    --background: var(--primary-red);
    --color: white;
    transform: translateY(-1px);
    transition: all 0.2s ease;
  }
}

/* ===========================================
   작은 모바일 화면 (320px ~ 480px)
   ========================================== */
@media (max-width: 480px) {
  .order-history-container {
    padding-bottom: 65px;
  }
  
  .order-content {
    flex-direction: column;
    gap: 12px;
  }
  
  .order-actions {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
}
</style>
