<template>
  <ion-page>
    <ion-header class="responsive-header">
      <ion-toolbar class="responsive-toolbar">
        <ion-title class="responsive-page-title">찜</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="favorites-container responsive-container">
        <!-- 로그인 유도 섹션 (미로그인 시) -->
        <div class="login-prompt spacing-xl" v-if="!isAuthenticated">
          <div class="prompt-card">
            <div class="empty-icon">
              <ion-icon :icon="heartOutline" class="large-icon"></ion-icon>
            </div>
            <h3 class="prompt-title responsive-subtitle">찜한 음식점을 확인하려면 로그인하세요</h3>
            <p class="prompt-desc responsive-small">로그인하고 찜한 음식점을 확인해보세요.</p>
            <ion-button 
              expand="block" 
              class="responsive-button login-prompt-button"
              @click="goToLogin"
            >
              로그인하기
            </ion-button>
          </div>
        </div>

        <!-- 찜 목록 (로그인 시) -->
        <div v-else>
          <!-- 빈 상태 -->
          <div class="empty-state spacing-xl" v-if="favorites.length === 0">
            <div class="empty-icon">
              <ion-icon :icon="heartOutline" class="large-icon"></ion-icon>
            </div>
            <h3 class="empty-title responsive-subtitle">찜한 음식점이 없어요</h3>
            <p class="empty-desc responsive-small">좋아하는 음식점을 찜해보세요!</p>
            <ion-button 
              expand="block" 
              class="responsive-button explore-button"
              @click="goToHome"
            >
              음식점 둘러보기
            </ion-button>
          </div>

          <!-- 찜 리스트 -->
          <div v-else>
            <!-- 정렬 옵션 -->
            <div class="sort-section spacing-sm">
              <ion-segment v-model="sortBy" class="sort-segment">
                <ion-segment-button value="recent">
                  <ion-label>최근 찜순</ion-label>
                </ion-segment-button>
                <ion-segment-button value="name">
                  <ion-label>이름순</ion-label>
                </ion-segment-button>
                <ion-segment-button value="rating">
                  <ion-label>별점순</ion-label>
                </ion-segment-button>
              </ion-segment>
            </div>

            <!-- 찜한 음식점 그리드 -->
            <div class="favorites-grid">
              <div class="favorite-item" v-for="restaurant in sortedFavorites" :key="restaurant.id">
                <div class="restaurant-image">
                  <div class="image-placeholder">
                    <ion-icon :icon="restaurant" class="restaurant-icon"></ion-icon>
                  </div>
                  <ion-button 
                    fill="clear" 
                    class="heart-button"
                    @click="toggleFavorite(restaurant)"
                  >
                    <ion-icon :icon="heart" class="heart-icon filled"></ion-icon>
                  </ion-button>
                </div>
                
                <div class="restaurant-info">
                  <h4 class="restaurant-name responsive-body">{{ restaurant.name }}</h4>
                  <p class="restaurant-desc responsive-small">{{ restaurant.description }}</p>
                  <div class="restaurant-meta">
                    <span class="rating responsive-small">⭐ {{ restaurant.rating }}</span>
                    <span class="delivery-time responsive-small">{{ restaurant.deliveryTime }}</span>
                  </div>
                  <div class="favorite-date responsive-small">
                    {{ formatDate(restaurant.favoriteDate) }} 찜
                  </div>
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
  IonSegment,
  IonSegmentButton,
  IonLabel,
  toastController
} from '@ionic/vue';
import { heartOutline, heart, restaurant } from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);
const sortBy = ref('recent');

// 임시 찜 데이터
const favorites = ref([
  {
    id: 1,
    name: '맛있는 피자집',
    description: '신선한 재료로 만든 맛있는 피자',
    rating: 4.5,
    deliveryTime: '30-40분',
    favoriteDate: '2024-05-24'
  },
  {
    id: 2,
    name: '치킨&맥주',
    description: '바삭한 치킨과 시원한 맥주',
    rating: 4.3,
    deliveryTime: '25-35분',
    favoriteDate: '2024-05-23'
  },
  {
    id: 3,
    name: '커피한잔',
    description: '향긋한 원두커피와 디저트',
    rating: 4.7,
    deliveryTime: '15-25분',
    favoriteDate: '2024-05-22'
  }
]);

const sortedFavorites = computed(() => {
  const sorted = [...favorites.value];
  
  switch (sortBy.value) {
    case 'name':
      return sorted.sort((a, b) => a.name.localeCompare(b.name));
    case 'rating':
      return sorted.sort((a, b) => b.rating - a.rating);
    case 'recent':
    default:
      return sorted.sort((a, b) => new Date(b.favoriteDate) - new Date(a.favoriteDate));
  }
});

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

const toggleFavorite = async (restaurant) => {
  // 찜 해제
  const index = favorites.value.findIndex(fav => fav.id === restaurant.id);
  if (index > -1) {
    favorites.value.splice(index, 1);
    
    const toast = await toastController.create({
      message: `${restaurant.name}을(를) 찜에서 제거했습니다.`,
      duration: 2000,
      color: 'medium',
      position: 'top',
    });
    await toast.present();
  }
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

.favorites-container {
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

.login-prompt-button, .explore-button {
  --background: var(--primary-red);
  --background-activated: var(--secondary-red);
  --background-hover: var(--secondary-red);
  --border-radius: 8px;
  --color: white;
  font-weight: 600;
  --box-shadow: none;
}

/* 정렬 섹션 */
.sort-section {
  margin-bottom: 20px;
}

.sort-segment {
  --background: var(--background-light);
  border-radius: 8px;
}

.sort-segment ion-segment-button {
  --indicator-color: var(--primary-red);
  --color: var(--text-secondary);
  --color-checked: var(--primary-red);
  font-size: 14px;
}

/* 찜 그리드 */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.favorite-item {
  background-color: var(--background-white);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;
}

.favorite-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.restaurant-image {
  position: relative;
  width: 100%;
  height: 120px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background-color: var(--background-light);
  display: flex;
  align-items: center;
  justify-content: center;
}

.restaurant-icon {
  font-size: 40px;
  color: var(--text-placeholder);
}

.heart-button {
  position: absolute;
  top: 8px;
  right: 8px;
  --background: rgba(255, 255, 255, 0.9);
  --border-radius: 50%;
  width: 36px;
  height: 36px;
  backdrop-filter: blur(4px);
}

.heart-icon {
  font-size: 20px;
}

.heart-icon.filled {
  color: var(--primary-red);
}

.restaurant-info {
  padding: 12px;
}

.restaurant-name {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.restaurant-desc {
  color: var(--text-secondary);
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.restaurant-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.rating, .delivery-time {
  color: var(--text-secondary);
}

.favorite-date {
  color: var(--text-placeholder);
  font-size: 12px;
}

/* ===========================================
   태블릿 반응형 스타일 (768px ~ 1023px)
   ========================================== */
@media (min-width: 768px) and (max-width: 1023px) {
  .favorites-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
  
  .restaurant-image {
    height: 140px;
  }
}

/* ===========================================
   데스크톱 반응형 스타일 (1024px+)
   ========================================== */
@media (min-width: 1024px) {
  .favorites-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
  }
  
  .restaurant-image {
    height: 160px;
  }
  
  .heart-button:hover {
    --background: rgba(255, 255, 255, 1);
    transform: scale(1.1);
    transition: all 0.2s ease;
  }
}

/* ===========================================
   작은 모바일 화면 (320px ~ 480px)
   ========================================== */
@media (max-width: 480px) {
  .favorites-container {
    padding-bottom: 65px;
  }
  
  .favorites-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .restaurant-image {
    height: 100px;
  }
  
  .restaurant-name {
    font-size: 14px;
  }
  
  .restaurant-info {
    font-size: 12px;
  }
}
</style>
