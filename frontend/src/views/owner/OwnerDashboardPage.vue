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

        <!-- 통계 카드들 -->
        <div class="stats-section">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-info">
                <h3>오늘 주문</h3>
                <p class="stat-number">{{ todayOrders }}</p>
                <span class="stat-change positive">+12% 어제 대비</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">💰</div>
              <div class="stat-info">
                <h3>오늘 매출</h3>
                <p class="stat-number">{{ formatCurrency(todaySales) }}</p>
                <span class="stat-change positive">+8% 어제 대비</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">⭐</div>
              <div class="stat-info">
                <h3>평점</h3>
                <p class="stat-number">{{ averageRating }}</p>
                <span class="stat-change neutral">리뷰 {{ reviewCount }}개</span>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">🏪</div>
              <div class="stat-info">
                <h3>내 가게</h3>
                <p class="stat-number">{{ storeCount }}</p>
                <span class="stat-change neutral">운영 중</span>
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

            <div class="action-card" @click="goToStores">
              <div class="action-icon">🏪</div>
              <h3>가게 관리</h3>
              <p>메뉴와 가게 정보를 관리하세요</p>
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
  toastController
} from '@ionic/vue';
import {
  logOutOutline,
  chevronForwardOutline
} from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();

// 사용자 정보
const user = computed(() => authStore.user);

// 대시보드 데이터 (실제로는 API에서 가져올 데이터)
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

const goToStores = () => {
  router.push('/owner/stores');
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

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  // 실제로는 여기서 API 호출하여 대시보드 데이터를 가져옵니다
  console.log('사장님 대시보드 로드 완료');
});
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
</style> 