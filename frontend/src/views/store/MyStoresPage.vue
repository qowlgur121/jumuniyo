<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>내 음식점 관리</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content class="my-stores-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="stores-header">
          <h1 class="responsive-title">내 음식점 목록</h1>
          <ion-button
            expand="block"
            fill="outline"
            size="large"
            class="register-button"
            @click="goToRegisterStore"
          >
            <ion-icon slot="start" name="add-circle"></ion-icon>
            새 음식점 등록
          </ion-button>
        </div>

        <!-- 로딩 스피너 -->
        <div v-if="isLoading" class="loading-container">
          <ion-spinner name="crescent"></ion-spinner>
          <p>음식점 목록을 불러오는 중...</p>
        </div>

        <!-- 음식점 목록이 없는 경우 -->
        <div v-else-if="stores.length === 0" class="empty-state">
          <ion-icon name="storefront-outline" class="empty-icon"></ion-icon>
          <h2>등록된 음식점이 없습니다</h2>
          <p>첫 번째 음식점을 등록하고 주문이요와 함께 사업을 시작해보세요!</p>
          <ion-button
            expand="block"
            size="large"
            class="register-button-primary"
            @click="goToRegisterStore"
          >
            <ion-icon slot="start" name="add-circle"></ion-icon>
            음식점 등록하기
          </ion-button>
        </div>

        <!-- 음식점 목록 -->
        <div v-else class="stores-list">
          <ion-card v-for="store in stores" :key="store.id" class="store-card">
            <ion-card-content>
              <div class="store-header">
                <div class="store-info">
                  <h2 class="store-name">{{ store.name }}</h2>
                  <p class="store-category">{{ store.category?.name }}</p>
                </div>
                <div class="store-status">
                  <ion-chip 
                    :color="getStatusColor(store)" 
                    class="status-chip"
                  >
                    {{ getStatusText(store) }}
                  </ion-chip>
                </div>
              </div>

              <div class="store-details">
                <div class="detail-item">
                  <ion-icon name="location-outline"></ion-icon>
                  <span>{{ store.address }}</span>
                </div>
                <div class="detail-item" v-if="store.phoneNumber">
                  <ion-icon name="call-outline"></ion-icon>
                  <span>{{ store.phoneNumber }}</span>
                </div>
                <div class="detail-item">
                  <ion-icon name="cash-outline"></ion-icon>
                  <span>최소주문: {{ formatCurrency(store.minimumOrderAmount) }}</span>
                </div>
                <div class="detail-item">
                  <ion-icon name="bicycle-outline"></ion-icon>
                  <span>배달비: {{ formatCurrency(store.deliveryFee) }}</span>
                </div>
              </div>

              <div class="store-stats" v-if="store.rating > 0 || store.reviewCount > 0">
                <div class="stat-item">
                  <ion-icon name="star" color="warning"></ion-icon>
                  <span>{{ store.rating || 0 }}</span>
                </div>
                <div class="stat-item">
                  <ion-icon name="chatbubble-outline"></ion-icon>
                  <span>리뷰 {{ store.reviewCount || 0 }}개</span>
                </div>
              </div>

              <div class="store-actions">
                <ion-button 
                  fill="outline" 
                  size="small"
                  @click="viewStore(store.id)"
                >
                  상세보기
                </ion-button>
                <ion-button 
                  fill="outline" 
                  size="small"
                  @click="editStore(store.id)"
                  :disabled="!store.isActive"
                >
                  수정
                </ion-button>
                <ion-button 
                  fill="outline" 
                  size="small" 
                  color="danger"
                  @click="confirmDeleteStore(store)"
                  :disabled="!store.isActive"
                >
                  삭제
                </ion-button>
              </div>
            </ion-card-content>
          </ion-card>
        </div>

        <!-- 무한 스크롤 -->
        <ion-infinite-scroll
          v-if="hasMore && !isLoading"
          @ionInfinite="loadMoreStores"
          threshold="100px"
        >
          <ion-infinite-scroll-content
            loading-spinner="bubbles"
            loading-text="더 많은 음식점을 불러오는 중..."
          >
          </ion-infinite-scroll-content>
        </ion-infinite-scroll>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonButton,
  IonCard,
  IonCardContent,
  IonChip,
  IonIcon,
  IonSpinner,
  IonInfiniteScroll,
  IonInfiniteScrollContent,
  toastController,
  alertController
} from '@ionic/vue';
import { storeApi } from '@/services/storeApi.js';

const router = useRouter();

// 상태 관리
const isLoading = ref(false);
const stores = ref([]);
const currentPage = ref(0);
const hasMore = ref(true);
const pageSize = 10;

// 마운트 시 데이터 로드
onMounted(async () => {
  await loadStores();
});

// 음식점 목록 로드
const loadStores = async (page = 0) => {
  if (page === 0) {
    isLoading.value = true;
  }

  try {
    const response = await storeApi.getMyStores(page, pageSize);
    const newStores = response.data.content;

    if (page === 0) {
      stores.value = newStores;
    } else {
      stores.value.push(...newStores);
    }

    hasMore.value = !response.data.last;
    currentPage.value = page;

  } catch (error) {
    console.error('음식점 목록 로드 실패:', error);
    showToast('음식점 목록을 불러오는데 실패했습니다.', 'danger');
  } finally {
    isLoading.value = false;
  }
};

// 더 많은 음식점 로드 (무한 스크롤)
const loadMoreStores = async (event) => {
  await loadStores(currentPage.value + 1);
  event.target.complete();
};

// 음식점 등록 페이지로 이동
const goToRegisterStore = () => {
  router.push('/store/register');
};

// 음식점 상세보기
const viewStore = (storeId) => {
  router.push(`/store/${storeId}`);
};

// 음식점 수정 페이지로 이동
const editStore = (storeId) => {
  router.push(`/store/${storeId}/edit`);
};

// 음식점 삭제 확인
const confirmDeleteStore = async (store) => {
  const alert = await alertController.create({
    header: '음식점 삭제',
    message: `'${store.name}' 음식점을 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`,
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '삭제',
        role: 'destructive',
        handler: () => deleteStore(store.id)
      }
    ]
  });

  await alert.present();
};

// 음식점 삭제
const deleteStore = async (storeId) => {
  try {
    await storeApi.deleteStore(storeId);
    showToast('음식점이 삭제되었습니다.', 'success');
    
    // 목록에서 제거
    stores.value = stores.value.filter(store => store.id !== storeId);
    
  } catch (error) {
    console.error('음식점 삭제 실패:', error);
    
    let errorMessage = '음식점 삭제 중 오류가 발생했습니다.';
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message;
    }
    
    showToast(errorMessage, 'danger');
  }
};

// 상태별 색상 반환
const getStatusColor = (store) => {
  if (!store.isActive) return 'medium';
  if (!store.isApproved) return 'warning';
  return 'success';
};

// 상태별 텍스트 반환
const getStatusText = (store) => {
  if (!store.isActive) return '비활성';
  if (!store.isApproved) return '승인대기';
  return '운영중';
};

// 통화 포맷팅
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount);
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
.my-stores-content {
  --background: var(--ion-color-light);
}

.stores-header {
  padding: 2rem 1rem;
  background: white;
  text-align: center;
  margin-bottom: 1rem;
}

.stores-header h1 {
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--yogiyo-primary);
}

.register-button {
  --border-color: var(--yogiyo-primary);
  --color: var(--yogiyo-primary);
  font-weight: 600;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: var(--ion-color-medium);
}

.loading-container ion-spinner {
  margin-bottom: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: var(--ion-color-medium);
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  color: var(--ion-color-medium);
}

.empty-state h2 {
  margin: 0 0 1rem 0;
  color: var(--ion-color-dark);
}

.empty-state p {
  margin: 0 0 2rem 0;
  line-height: 1.5;
}

.register-button-primary {
  --background: var(--yogiyo-primary);
  --color: white;
  font-weight: 600;
}

.stores-list {
  padding: 0 1rem 2rem;
}

.store-card {
  margin-bottom: 1rem;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.store-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.store-info {
  flex: 1;
}

.store-name {
  margin: 0 0 0.25rem 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: var(--ion-color-dark);
}

.store-category {
  margin: 0;
  color: var(--ion-color-medium);
  font-size: 0.9rem;
}

.status-chip {
  font-size: 0.8rem;
  font-weight: 600;
}

.store-details {
  margin-bottom: 1rem;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: var(--ion-color-dark);
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item ion-icon {
  margin-right: 0.5rem;
  color: var(--ion-color-medium);
  width: 16px;
}

.store-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: var(--ion-color-light);
  border-radius: 8px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.9rem;
  color: var(--ion-color-dark);
}

.store-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--ion-color-light);
}

.store-actions ion-button {
  flex: 1;
  min-width: 80px;
}

/* 반응형 디자인 */
@media (max-width: 767px) {
  .stores-header {
    padding: 1.5rem 1rem;
  }
  
  .stores-header h1 {
    font-size: 1.3rem;
  }
  
  .empty-state {
    padding: 2rem 1rem;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
  
  .store-header {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .store-actions {
    flex-direction: column;
  }
  
  .store-actions ion-button {
    width: 100%;
  }
}

@media (min-width: 768px) {
  .stores-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 1rem;
    padding: 0 2rem 2rem;
  }
  
  .store-card {
    margin-bottom: 0;
  }
}

@media (min-width: 1024px) {
  .stores-list {
    grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
  }
}
</style> 