<template>
  <ion-page>
    <ion-header class="responsive-header">
      <ion-toolbar class="responsive-toolbar">
        <ion-title class="responsive-page-title">마이</ion-title>
      </ion-toolbar>
    </ion-header>
    
    <ion-content :fullscreen="true" class="safe-area-padding">
      <div class="profile-container responsive-container">
        <!-- 로그인된 상태 -->
        <div v-if="isAuthenticated">
          <!-- 사용자 정보 카드 -->
          <div class="user-info-card spacing-lg">
            <div class="user-avatar">
              <ion-icon :icon="personCircleOutline" class="avatar-icon"></ion-icon>
            </div>
            <div class="user-details">
              <h2 class="user-name responsive-subtitle">{{ authStore.userInfo?.nickname || '사용자' }}</h2>
              <p class="user-email responsive-small">{{ authStore.userInfo?.email || '이메일 없음' }}</p>
              <ion-chip class="user-role-chip">
                <ion-label>{{ getRoleLabel(authStore.userInfo?.role) }}</ion-label>
              </ion-chip>
            </div>
          </div>

          <!-- 메뉴 리스트 -->
          <div class="menu-section spacing-md">
            <div class="menu-item" @click="goToProfile">
              <div class="menu-icon">
                <ion-icon :icon="personOutline"></ion-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title responsive-body">개인정보 수정</span>
                <span class="menu-desc responsive-small">프로필 정보를 변경할 수 있습니다</span>
              </div>
              <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
            </div>

            <div class="menu-item" @click="goToNotifications">
              <div class="menu-icon">
                <ion-icon :icon="notificationsOutline"></ion-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title responsive-body">알림 설정</span>
                <span class="menu-desc responsive-small">주문 상태 및 혜택 알림을 설정합니다</span>
              </div>
              <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
            </div>

            <div class="menu-item" @click="goToCoupons">
              <div class="menu-icon">
                <ion-icon :icon="ticketOutline"></ion-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title responsive-body">쿠폰함</span>
                <span class="menu-desc responsive-small">사용 가능한 쿠폰을 확인하세요</span>
              </div>
              <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
            </div>

            <div class="menu-item" @click="goToSupport">
              <div class="menu-icon">
                <ion-icon :icon="helpCircleOutline"></ion-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title responsive-body">고객센터</span>
                <span class="menu-desc responsive-small">문의사항이나 도움이 필요하시면 연락하세요</span>
              </div>
              <ion-icon :icon="chevronForwardOutline" class="menu-arrow"></ion-icon>
            </div>

            <div class="menu-item logout-item" @click="handleLogout">
              <div class="menu-icon logout-icon">
                <ion-icon :icon="logOutOutline"></ion-icon>
              </div>
              <div class="menu-content">
                <span class="menu-title responsive-body logout-text">로그아웃</span>
                <span class="menu-desc responsive-small">계정에서 안전하게 로그아웃합니다</span>
              </div>
              <ion-spinner v-if="authStore.isLoading" name="crescent" class="logout-spinner"></ion-spinner>
            </div>
          </div>
        </div>

        <!-- 로그인되지 않은 상태 -->
        <div class="login-prompt spacing-xl" v-else>
          <div class="prompt-card">
            <div class="empty-icon">
              <ion-icon :icon="personCircleOutline" class="large-icon"></ion-icon>
            </div>
            <h3 class="prompt-title responsive-subtitle">로그인이 필요합니다</h3>
            <p class="prompt-desc responsive-small">주문이요의 다양한 서비스를 이용하려면 로그인해주세요.</p>
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
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { 
  personCircleOutline, 
  personOutline, 
  notificationsOutline, 
  helpCircleOutline, 
  logOutOutline,
  chevronForwardOutline,
  ticketOutline
} from 'ionicons/icons';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonIcon,
  IonChip,
  IonLabel,
  IonButton,
  IonSpinner,
  toastController,
  alertController
} from '@ionic/vue';

const router = useRouter();
const authStore = useAuthStore();

const isAuthenticated = computed(() => authStore.isAuthenticated);

const getRoleLabel = (role) => {
  switch (role) {
    case 'ROLE_USER':
      return '일반 사용자';
    case 'ROLE_OWNER':
      return '사장님';
    case 'ROLE_ADMIN':
      return '관리자';
    default:
      return '사용자';
  }
};

const handleLogout = async () => {
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
        handler: async () => {
          await authStore.logout();
          
          const toast = await toastController.create({
            message: '로그아웃되었습니다.',
            duration: 2000,
            color: 'success',
            position: 'top',
          });
          await toast.present();
          
          // 로그인 페이지로 이동
          router.push('/auth/login');
        },
      },
    ],
  });
  await alert.present();
};

const goToLogin = () => {
  router.push('/auth/login');
};

const goToProfile = async () => {
  const toast = await toastController.create({
    message: '개인정보 수정 기능은 준비 중입니다.',
    duration: 2000,
    color: 'medium',
    position: 'top',
  });
  await toast.present();
};

const goToNotifications = async () => {
  const toast = await toastController.create({
    message: '알림 설정 기능은 준비 중입니다.',
    duration: 2000,
    color: 'medium',
    position: 'top',
  });
  await toast.present();
};

const goToCoupons = async () => {
  const toast = await toastController.create({
    message: '쿠폰함 기능은 준비 중입니다.',
    duration: 2000,
    color: 'medium',
    position: 'top',
  });
  await toast.present();
};

const goToSupport = async () => {
  const toast = await toastController.create({
    message: '고객센터 기능은 준비 중입니다.',
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
}

.profile-container {
  padding-top: 16px;
  padding-bottom: 75px;
}

/* 사용자 정보 카드 */
.user-info-card {
  background-color: var(--background-white);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 64px;
  color: var(--primary-red);
}

.user-details {
  flex: 1;
}

.user-name {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.user-email {
  color: var(--text-secondary);
  margin: 0 0 12px 0;
}

.user-role-chip {
  --background: var(--primary-red);
  --color: white;
  font-size: 12px;
}

/* 메뉴 섹션 */
.menu-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  background-color: var(--background-white);
  border: 1px solid var(--border-gray);
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: var(--background-light);
  transform: translateY(-1px);
}

.menu-icon {
  width: 40px;
  height: 40px;
  background-color: var(--background-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.menu-icon ion-icon {
  font-size: 20px;
  color: var(--text-primary);
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-title {
  font-weight: 600;
  color: var(--text-primary);
}

.menu-desc {
  color: var(--text-secondary);
  line-height: 1.4;
}

.menu-arrow {
  font-size: 20px;
  color: var(--text-placeholder);
}

/* 로그아웃 메뉴 스타일 */
.logout-item {
  border-color: #ffebee;
}

.logout-item:hover {
  background-color: #ffebee;
}

.logout-icon {
  background-color: #ffebee;
}

.logout-icon ion-icon {
  color: #d32f2f;
}

.logout-text {
  color: #d32f2f;
}

.logout-spinner {
  font-size: 20px;
  color: #d32f2f;
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

.prompt-title {
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.prompt-desc {
  color: var(--text-secondary);
  margin: 0 0 24px 0;
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
  .user-info-card {
    padding: 28px;
  }
  
  .avatar-icon {
    font-size: 72px;
  }
  
  .menu-item {
    padding: 20px;
  }
  
  .menu-icon {
    width: 48px;
    height: 48px;
  }
  
  .menu-icon ion-icon {
    font-size: 24px;
  }
}

/* ===========================================
   데스크톱 반응형 스타일 (1024px+)
   ========================================== */
@media (min-width: 1024px) {
  .user-info-card {
    padding: 32px;
  }
  
  .avatar-icon {
    font-size: 80px;
  }
  
  .menu-item {
    padding: 24px;
  }
  
  .menu-icon {
    width: 56px;
    height: 56px;
  }
  
  .menu-icon ion-icon {
    font-size: 28px;
  }
  
  .menu-item:hover .menu-arrow {
    color: var(--primary-red);
    transform: translateX(4px);
    transition: all 0.2s ease;
  }
}

/* ===========================================
   작은 모바일 화면 (320px ~ 480px)
   ========================================== */
@media (max-width: 480px) {
  .profile-container {
    padding-bottom: 65px;
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }
  
  .user-details {
    width: 100%;
  }
  
  .menu-item {
    padding: 12px;
  }
  
  .menu-icon {
    width: 36px;
    height: 36px;
  }
  
  .menu-icon ion-icon {
    font-size: 18px;
  }
  
  .menu-content {
    gap: 2px;
  }
  
  .menu-desc {
    display: none;
  }
}
</style> 