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
          <!-- 빈 상태 -->
          <div class="empty-state spacing-xl" v-if="orders.length === 0">
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
            <div class="order-item" v-for="order in orders" :key="order.id">
              <div class="order-header">
                <div class="order-date responsive-small">{{ formatDate(order.date) }}</div>
                <div class="order-status" :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </div>
              </div>
              
              <div class="order-content">
                <div class="restaurant-info">
                  <h4 class="restaurant-name responsive-body">{{ order.restaurantName }}</h4>
                  <p class="order-items responsive-small">{{ order.items.join(', ') }}</p>
                </div>
                
                <div class="order-actions">
                  <div class="order-price responsive-body">{{ formatPrice(order.totalPrice) }}원</div>
                  <ion-button 
                    fill="outline" 
                    size="small" 
                    class="reorder-button"
                    @click="reorder(order)"
                    v-if="order.status === 'delivered'"
                  >
                    재주문
                  </ion-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { 
  IonPage, 
  IonHeader, 
  IonToolbar, 
  IonTitle, 
  IonContent, 
  IonButton,
  IonIcon,
  toastController
} from '@ionic/vue';
import { receiptOutline } from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);

// 임시 주문내역 데이터
const orders = ref([
  {
    id: 1,
    date: '2024-05-24',
    restaurantName: '맛있는 피자집',
    items: ['페퍼로니 피자 L', '콜라 1.25L'],
    totalPrice: 28000,
    status: 'delivered'
  },
  {
    id: 2,
    date: '2024-05-23',
    restaurantName: '치킨&맥주',
    items: ['후라이드 치킨', '맥주 500ml x2'],
    totalPrice: 25000,
    status: 'delivered'
  }
]);

const goToLogin = () => {
  router.push('/auth/login');
};

const goToHome = () => {
  router.push('/tabs/tab1');
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getMonth() + 1}월 ${date.getDate()}일`;
};

const formatPrice = (price) => {
  return price.toLocaleString();
};

const getStatusText = (status) => {
  const statusMap = {
    'ordered': '주문완료',
    'preparing': '조리중',
    'delivering': '배달중',
    'delivered': '배달완료',
    'cancelled': '주문취소'
  };
  return statusMap[status] || status;
};

const getStatusClass = (status) => {
  return `status-${status}`;
};

const reorder = async (order) => {
  const toast = await toastController.create({
    message: `${order.restaurantName}에서 재주문 기능은 준비 중입니다.`,
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
.login-prompt, .empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.prompt-card {
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

.status-delivered {
  background-color: #e8f5e8;
  color: #2d7d32;
}

.status-preparing {
  background-color: #fff3e0;
  color: #ef6c00;
}

.status-delivering {
  background-color: #e3f2fd;
  color: #1976d2;
}

.status-cancelled {
  background-color: #ffebee;
  color: #d32f2f;
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
