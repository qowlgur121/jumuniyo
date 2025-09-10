<template>
  <ion-page>
    <ion-header class="responsive-header">
      <ion-toolbar class="responsive-toolbar">
        <ion-title class="responsive-page-title">홈</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" class="responsive-header-button">
            <ion-icon :icon="notifications" slot="icon-only"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>
    
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="home-container responsive-container">
        <!-- 환영 섹션 -->
        <div class="welcome-section spacing-md">
          <h2 class="welcome-title responsive-title">안녕하세요!</h2>
          <p class="welcome-subtitle responsive-subtitle">오늘은 무엇을 드시고 싶으신가요?</p>
        </div>

        <!-- 검색 섹션 -->
        <div class="search-section spacing-sm">
          <ion-searchbar 
            placeholder="음식, 음식점 검색"
            class="responsive-searchbar"
            show-clear-button="focus"
            @ionFocus="handleSearchFocus"
            readonly
          ></ion-searchbar>
        </div>

        <!-- 카테고리 섹션 -->
        <div class="category-section spacing-md">
          <h3 class="section-title responsive-body">카테고리</h3>
          <div class="category-grid">
            <div 
              class="category-item" 
              v-for="category in displayedCategories" 
              :key="category.id"
              @click="handleCategoryClick(category)"
            >
              <div class="category-icon">
                <ion-icon :icon="category.icon" class="responsive-tab-icon"></ion-icon>
              </div>
              <span class="category-label responsive-small">{{ category.name }}</span>
            </div>
            <!-- 더보기/접기 버튼 -->
            <div class="category-item more-button" @click="toggleShowAllCategories">
              <div class="category-icon more-icon">
                <ion-icon :icon="showAllCategories ? chevronUpOutline : chevronDownOutline" class="responsive-tab-icon"></ion-icon>
              </div>
              <span class="category-label responsive-small">{{ showAllCategories ? '접기' : '더보기' }}</span>
            </div>
          </div>
        </div>

        <!-- 추천 음식점 섹션 -->
        <div class="restaurant-section spacing-md">
          <h3 class="section-title responsive-body">추천 음식점</h3>
          <div class="restaurant-list">
            <div class="restaurant-card" v-for="restaurant in restaurants" :key="restaurant.id">
              <div class="restaurant-image">
                <div class="image-placeholder">
                  <ion-icon :icon="restaurant" class="restaurant-icon"></ion-icon>
                </div>
              </div>
              <div class="restaurant-info">
                <h4 class="restaurant-name responsive-body">{{ restaurant.name }}</h4>
                <p class="restaurant-desc responsive-small">{{ restaurant.description }}</p>
                <div class="restaurant-meta">
                  <span class="rating responsive-small">⭐ {{ restaurant.rating }}</span>
                  <span class="delivery-time responsive-small">{{ restaurant.deliveryTime }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 로그인 유도 섹션 (미로그인 시) -->
        <div class="login-prompt spacing-lg" v-if="!isAuthenticated">
          <div class="prompt-card">
            <h3 class="prompt-title responsive-subtitle">더 많은 혜택을 받아보세요!</h3>
            <p class="prompt-desc responsive-small">로그인하고 맞춤 음식점을 추천받아보세요.</p>
            <ion-button 
              expand="block" 
              class="responsive-button login-prompt-button"
              @click="goToLogin"
            >
              로그인하기
            </ion-button>
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
  IonButtons,
  IonIcon,
  IonSearchbar,
  toastController
} from '@ionic/vue';
import { 
  notifications, 
  restaurant, 
  pizza, 
  cafe, 
  fastFood,
  fish,
  iceCream,
  wine,
  gridOutline,
  restaurantOutline,
  nutrition,
  leaf,
  home,
  storefront,
  star,
  chevronDownOutline,
  chevronUpOutline,
  barbellOutline,
  eggOutline,
  wineOutline,
  roseOutline,
  flowerOutline
} from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();
const showAllCategories = ref(false);

const isAuthenticated = computed(() => authStore.isAuthenticated);

// 전체 카테고리 목록 (요기요 스타일로 개선)
const allCategories = [
  { id: 1, name: '전체', icon: gridOutline },
  { id: 2, name: '카페/디저트', icon: cafe },
  { id: 3, name: '치킨', icon: fastFood },
  { id: 4, name: '한식', icon: restaurant },
  { id: 5, name: '중국집', icon: restaurantOutline },
  { id: 6, name: '분식', icon: nutrition },
  { id: 7, name: '피자/양식', icon: pizza },
  { id: 8, name: '버거', icon: fastFood },
  { id: 9, name: '일식/돈까스', icon: fish },
  { id: 10, name: '짬뽕', icon: restaurant },
  { id: 11, name: '회/초밥', icon: fish },
  { id: 12, name: '족발/보쌈', icon: restaurant },
  { id: 13, name: '고기/구이', icon: restaurant },
  { id: 14, name: '샌드위치', icon: fastFood },
  { id: 15, name: '샐러드', icon: leaf },
  { id: 16, name: '도시락/죽', icon: restaurant },
  { id: 17, name: '아시안', icon: restaurantOutline },
  { id: 18, name: '1인분주문', icon: home },
  { id: 19, name: '프랜차이즈', icon: storefront },
  { id: 20, name: '신규맛집', icon: star }
];

// 표시할 카테고리 계산 (요기요처럼 더보기 포함 10개)
const displayedCategories = computed(() => {
  if (showAllCategories.value) {
    return allCategories.slice(0, -1); // 마지막 항목 제외하고 모두 표시
  } else {
    return allCategories.slice(0, 9); // 처음 9개만 표시 (더보기 버튼 포함하면 10개)
  }
});

const restaurants = [
  {
    id: 1,
    name: '맛있는 피자집',
    description: '신선한 재료로 만든 맛있는 피자',
    rating: 4.5,
    deliveryTime: '30-40분'
  },
  {
    id: 2,
    name: '치킨&맥주',
    description: '바삭한 치킨과 시원한 맥주',
    rating: 4.3,
    deliveryTime: '25-35분'
  },
  {
    id: 3,
    name: '커피한잔',
    description: '향긋한 원두커피와 디저트',
    rating: 4.7,
    deliveryTime: '15-25분'
  }
];

const toggleShowAllCategories = () => {
  showAllCategories.value = !showAllCategories.value;
};

const handleCategoryClick = async (category) => {
  const toast = await toastController.create({
    message: `${category.name} 카테고리 기능은 준비 중입니다.`,
    duration: 2000,
    color: 'medium',
    position: 'top',
  });
  await toast.present();
};

const goToLogin = () => {
  router.push('/auth/login');
};

const handleSearchFocus = async () => {
  const toast = await toastController.create({
    message: '검색 기능은 준비 중입니다. 곧 만나보실 수 있어요!',
    duration: 2000,
    color: 'medium',
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

.responsive-header-button {
  --color: var(--text-primary);
}

.home-container {
  padding-top: 8px;
  padding-bottom: 75px; /* 탭 바 공간 확보 */
}

/* 환영 섹션 */
.welcome-section {
  text-align: center;
  padding: 16px 0 8px 0;
}

.welcome-title {
  color: var(--text-primary);
  margin: 0 0 6px 0;
  font-size: 24px;
}

.welcome-subtitle {
  color: var(--text-secondary);
  margin: 0;
  font-size: 14px;
}

/* 검색 섹션 */
.search-section {
  width: 100%;
}

.responsive-searchbar {
  --background: var(--background-light);
  --color: var(--text-primary);
  --placeholder-color: var(--text-placeholder);
  --icon-color: var(--text-secondary);
  --border-radius: 12px;
  padding: 0;
}

/* 카테고리 섹션 */
.section-title {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  font-size: 16px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  padding: 0 4px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  padding: 8px 2px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.category-item:hover {
  background-color: var(--background-light);
  transform: translateY(-2px);
}

.category-icon {
  width: 40px;
  height: 40px;
  background-color: var(--primary-red);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.category-icon ion-icon {
  color: white;
  font-size: 20px;
}

.category-label {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 11px;
  line-height: 1.2;
  word-break: keep-all;
}

/* 더보기 버튼 스타일 */
.more-button .category-icon {
  background-color: var(--background-light);
  border: 2px solid var(--border-gray);
}

.more-button .category-icon ion-icon {
  color: var(--text-secondary);
}

.more-button .category-label {
  color: var(--text-secondary);
}

.more-button:hover .category-icon {
  background-color: var(--primary-red);
  border-color: var(--primary-red);
}

.more-button:hover .category-icon ion-icon {
  color: white;
}

.more-button:hover .category-label {
  color: var(--primary-red);
}

/* 음식점 섹션 */
.restaurant-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.restaurant-card {
  display: flex;
  background-color: var(--background-white);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.restaurant-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.restaurant-image {
  width: 70px;
  height: 70px;
  margin-right: 14px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  background-color: var(--background-light);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.restaurant-icon {
  font-size: 28px;
  color: var(--text-placeholder);
}

.restaurant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.restaurant-name {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 4px 0;
  font-size: 15px;
}

.restaurant-desc {
  color: var(--text-secondary);
  margin: 0 0 8px 0;
  font-size: 13px;
}

.restaurant-meta {
  display: flex;
  gap: 16px;
}

.rating, .delivery-time {
  color: var(--text-secondary);
  font-size: 12px;
}

/* 로그인 유도 섹션 */
.login-prompt {
  margin-top: 20px;
}

.prompt-card {
  background-color: var(--background-light);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.prompt-title {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
  font-size: 16px;
}

.prompt-desc {
  color: var(--text-secondary);
  margin: 0 0 16px 0;
  font-size: 13px;
}

.login-prompt-button {
  --background: var(--primary-red);
  --background-activated: var(--secondary-red);
  --background-hover: var(--secondary-red);
  --border-radius: 8px;
  --color: white;
  font-weight: 600;
  --box-shadow: none;
}

/* ===========================================
   태블릿 반응형 스타일 (768px ~ 1023px)
   ========================================== */
@media (min-width: 768px) and (max-width: 1023px) {
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }
  
  .category-icon {
    width: 52px;
    height: 52px;
  }
  
  .category-icon ion-icon {
    font-size: 26px;
  }
  
  .category-label {
    font-size: 13px;
  }
  
  .restaurant-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .restaurant-card {
    flex-direction: column;
  }
  
  .restaurant-image {
    width: 100%;
    height: 100px;
    margin-right: 0;
    margin-bottom: 12px;
  }
}

/* ===========================================
   데스크톱 반응형 스타일 (1024px+)
   ========================================== */
@media (min-width: 1024px) {
  .welcome-section {
    padding: 24px 0 16px 0;
  }
  
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 20px;
    padding: 0 8px;
  }
  
  .category-icon {
    width: 56px;
    height: 56px;
  }
  
  .category-icon ion-icon {
    font-size: 28px;
  }
  
  .category-label {
    font-size: 14px;
  }
  
  .restaurant-list {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }
  
  .restaurant-card {
    flex-direction: column;
  }
  
  .restaurant-image {
    width: 100%;
    height: 120px;
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .prompt-card {
    max-width: 400px;
    margin: 0 auto;
  }
}

/* ===========================================
   작은 모바일 화면 (320px ~ 480px)
   ========================================== */
@media (max-width: 480px) {
  .home-container {
    padding-bottom: 65px;
  }
  
  .category-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 6px;
  }
  
  .category-item {
    padding: 6px 1px;
  }
  
  .category-icon {
    width: 36px;
    height: 36px;
  }
  
  .category-icon ion-icon {
    font-size: 18px;
  }
  
  .category-label {
    font-size: 10px;
  }
  
  .restaurant-image {
    width: 60px;
    height: 60px;
  }
  
  .restaurant-icon {
    font-size: 24px;
  }
}
</style>
