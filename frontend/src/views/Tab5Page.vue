<template>
  <ion-page>
    <ion-header class="responsive-header">
      <ion-toolbar class="responsive-toolbar">
        <ion-title class="responsive-page-title">마이</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="my-page-container responsive-container">
        <!-- 로그인 유도 섹션 (미로그인 시) -->
        <div class="login-prompt spacing-xl" v-if="!isAuthenticated">
          <div class="prompt-card">
            <div class="empty-icon">
              <ion-icon :icon="person" class="large-icon"></ion-icon>
            </div>
            <h3 class="prompt-title responsive-subtitle">로그인하고 더 많은 서비스를 이용해보세요</h3>
            <p class="prompt-desc responsive-small">주문내역, 쿠폰, 리뷰 등을 확인할 수 있어요</p>
            <ion-button 
              expand="block" 
              class="responsive-button login-prompt-button"
              @click="goToLogin"
            >
              로그인하기
            </ion-button>
            <ion-button 
              expand="block" 
              fill="outline"
              class="responsive-button signup-button spacing-sm"
              @click="goToSignUp"
            >
              회원가입하기
            </ion-button>
          </div>
        </div>

        <!-- 로그인 후 마이페이지 -->
        <div v-else>
          <!-- 사용자 프로필 헤더 -->
          <div class="profile-header spacing-lg">
            <div class="profile-info">
              <div class="profile-avatar">
                <ion-icon :icon="person" class="avatar-icon"></ion-icon>
              </div>
              <div class="profile-text">
                <h3 class="profile-name responsive-body">{{ userProfile.nickname }}</h3>
                <p class="profile-email responsive-small">{{ userProfile.email }}</p>
              </div>
            </div>
            <ion-button 
              fill="outline" 
              size="small" 
              class="edit-profile-button"
              @click="editProfile"
            >
              프로필 수정
            </ion-button>
          </div>

          <!-- 주요 메뉴 -->
          <div class="menu-section spacing-lg">
            <div class="menu-grid">
              <div class="menu-item" @click="goToOrderHistory">
                <ion-icon :icon="receiptOutline" class="menu-icon"></ion-icon>
                <span class="menu-label responsive-small">주문내역</span>
              </div>
              <div class="menu-item" @click="goToFavorites">
                <ion-icon :icon="heartOutline" class="menu-icon"></ion-icon>
                <span class="menu-label responsive-small">찜</span>
              </div>
              <div class="menu-item" @click="showCoupons">
                <ion-icon :icon="ticketOutline" class="menu-icon"></ion-icon>
                <span class="menu-label responsive-small">쿠폰함</span>
              </div>
              <div class="menu-item" @click="showPoints">
                <ion-icon :icon="walletOutline" class="menu-icon"></ion-icon>
                <span class="menu-label responsive-small">포인트</span>
              </div>
            </div>
          </div>

          <!-- 설정 메뉴 -->
          <div class="settings-section spacing-lg">
            <h4 class="section-title responsive-body">설정</h4>
            <div class="settings-list">
              <div class="setting-item" @click="showNotificationSettings">
                <ion-icon :icon="notificationsOutline" class="setting-icon"></ion-icon>
                <span class="setting-label responsive-body">알림 설정</span>
                <ion-icon :icon="chevronForward" class="setting-arrow"></ion-icon>
              </div>
              <div class="setting-item" @click="showAddressBook">
                <ion-icon :icon="locationOutline" class="setting-icon"></ion-icon>
                <span class="setting-label responsive-body">주소록</span>
                <ion-icon :icon="chevronForward" class="setting-arrow"></ion-icon>
              </div>
              <div class="setting-item" @click="showPaymentMethods">
                <ion-icon :icon="cardOutline" class="setting-icon"></ion-icon>
                <span class="setting-label responsive-body">결제수단</span>
                <ion-icon :icon="chevronForward" class="setting-arrow"></ion-icon>
              </div>
              <div class="setting-item" @click="showHelp">
                <ion-icon :icon="helpCircleOutline" class="setting-icon"></ion-icon>
                <span class="setting-label responsive-body">고객센터</span>
                <ion-icon :icon="chevronForward" class="setting-arrow"></ion-icon>
              </div>
            </div>
          </div>

          <!-- 로그아웃 버튼 -->
          <div class="logout-section spacing-xl">
            <ion-button 
              expand="block" 
              fill="outline"
              class="responsive-button logout-button"
              @click="logout"
            >
              로그아웃
            </ion-button>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import { computed } from 'vue';
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
  toastController,
  alertController
} from '@ionic/vue';
import { 
  person, 
  receiptOutline, 
  heartOutline, 
  ticketOutline,
  walletOutline,
  notificationsOutline,
  locationOutline,
  cardOutline,
  helpCircleOutline,
  chevronForward
} from 'ionicons/icons';

const router = useRouter();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);
const userProfile = computed(() => authStore.user || {
  nickname: '사용자',
  email: 'user@example.com'
});

const goToLogin = () => {
  router.push('/auth/login');
};

const goToSignUp = () => {
  router.push('/auth/signup');
};

const goToOrderHistory = () => {
  router.push('/tabs/tab2');
};

const goToFavorites = () => {
  router.push('/tabs/tab3');
};

const editProfile = async () => {
  const toast = await toastController.create({
    message: '프로필 수정 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showCoupons = async () => {
  const toast = await toastController.create({
    message: '쿠폰함 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showPoints = async () => {
  const toast = await toastController.create({
    message: '포인트 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showNotificationSettings = async () => {
  const toast = await toastController.create({
    message: '알림 설정 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showAddressBook = async () => {
  const toast = await toastController.create({
    message: '주소록 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showPaymentMethods = async () => {
  const toast = await toastController.create({
    message: '결제수단 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const showHelp = async () => {
  const toast = await toastController.create({
    message: '고객센터 기능은 준비 중입니다.',
    duration: 2000,
    color: 'primary',
    position: 'top',
  });
  await toast.present();
};

const logout = async () => {
  const alert = await alertController.create({
    header: '로그아웃',
    message: '정말 로그아웃하시겠습니까?',
    buttons: [
      {
        text: '취소',
        role: 'cancel',
      },
      {
        text: '로그아웃',
        handler: () => {
          authStore.logout();
          router.push('/tabs/tab1');
        },
      },
    ],
  });

  await alert.present();
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

.my-page-container {
  padding-top: 16px;
  padding-bottom: 75px;
}

/* 로그인 유도 */
.login-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.prompt-card {
  background-color: var(--background-light);
  border-radius: 16px;
  padding: 40px 32px;
  text-align: center;
  width: 100%;
  max-width: 320px;
}

.empty-icon {
  margin-bottom: 20px;
}

.large-icon {
  font-size: 80px;
  color: var(--text-placeholder);
}

.prompt-title {
  color: var(--text-primary);
  margin-bottom: 12px;
}

.prompt-desc {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.login-prompt-button {
  --background: var(--primary-red);
  --color: white;
  margin-bottom: 12px;
}

.signup-button {
  --border-color: var(--primary-red);
  --color: var(--primary-red);
}

/* 프로필 헤더 */
.profile-header {
  background-color: var(--background-light);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.profile-avatar {
  width: 60px;
  height: 60px;
  background-color: var(--primary-red);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-icon {
  font-size: 32px;
  color: white;
}

.profile-name {
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile-email {
  color: var(--text-secondary);
}

.edit-profile-button {
  --border-color: var(--primary-red);
  --color: var(--primary-red);
}

/* 주요 메뉴 */
.menu-section {
  padding: 0 8px;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.menu-item {
  background-color: var(--background-light);
  border-radius: 12px;
  padding: 20px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: var(--border-gray);
  transform: translateY(-2px);
}

.menu-icon {
  font-size: 28px;
  color: var(--primary-red);
  margin-bottom: 8px;
}

.menu-label {
  color: var(--text-primary);
  font-weight: 500;
}

/* 설정 섹션 */
.section-title {
  color: var(--text-primary);
  margin-bottom: 16px;
  font-weight: 600;
}

.settings-list {
  background-color: var(--background-light);
  border-radius: 16px;
  overflow: hidden;
}

.setting-item {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid var(--border-gray);
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item:hover {
  background-color: var(--border-gray);
}

.setting-icon {
  font-size: 24px;
  color: var(--text-secondary);
  margin-right: 16px;
}

.setting-label {
  flex: 1;
  color: var(--text-primary);
}

.setting-arrow {
  font-size: 20px;
  color: var(--text-placeholder);
}

/* 로그아웃 버튼 */
.logout-button {
  --border-color: var(--text-placeholder);
  --color: var(--text-secondary);
}

/* 반응형 디자인 */
@media (max-width: 480px) {
  .menu-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .menu-item {
    padding: 16px 8px;
  }
  
  .menu-icon {
    font-size: 24px;
  }
  
  .profile-header {
    padding: 20px;
  }
  
  .profile-avatar {
    width: 50px;
    height: 50px;
  }
  
  .avatar-icon {
    font-size: 28px;
  }
}

@media (min-width: 768px) {
  .menu-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
  }
  
  .menu-item {
    padding: 24px 16px;
  }
  
  .setting-item {
    padding: 24px 28px;
  }
}
</style> 