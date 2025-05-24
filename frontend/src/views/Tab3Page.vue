<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-title>프로필</ion-title>
      </ion-toolbar>
    </ion-header>
    <ion-content :fullscreen="true">
      <ion-header collapse="condense">
        <ion-toolbar>
          <ion-title size="large">프로필</ion-title>
        </ion-toolbar>
      </ion-header>

      <div class="profile-container" v-if="authStore.isAuthenticated">
        <!-- 사용자 정보 카드 -->
        <ion-card class="user-info-card">
          <ion-card-content>
            <div class="user-avatar">
              <ion-icon :icon="personCircleOutline" size="large"></ion-icon>
            </div>
            <div class="user-details">
              <h2>{{ authStore.userInfo?.nickname || '사용자' }}</h2>
              <p>{{ authStore.userInfo?.email || '이메일 없음' }}</p>
              <ion-chip color="primary">
                <ion-label>{{ getRoleLabel(authStore.userInfo?.role) }}</ion-label>
              </ion-chip>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- 메뉴 리스트 -->
        <ion-list>
          <ion-item button>
            <ion-icon :icon="personOutline" slot="start"></ion-icon>
            <ion-label>개인정보 수정</ion-label>
            <ion-icon :icon="chevronForwardOutline" slot="end"></ion-icon>
          </ion-item>

          <ion-item button>
            <ion-icon :icon="notificationsOutline" slot="start"></ion-icon>
            <ion-label>알림 설정</ion-label>
            <ion-icon :icon="chevronForwardOutline" slot="end"></ion-icon>
          </ion-item>

          <ion-item button>
            <ion-icon :icon="helpCircleOutline" slot="start"></ion-icon>
            <ion-label>고객센터</ion-label>
            <ion-icon :icon="chevronForwardOutline" slot="end"></ion-icon>
          </ion-item>

          <ion-item button @click="handleLogout">
            <ion-icon :icon="logOutOutline" slot="start" color="danger"></ion-icon>
            <ion-label color="danger">로그아웃</ion-label>
            <ion-spinner v-if="authStore.isLoading" name="crescent" slot="end"></ion-spinner>
          </ion-item>
        </ion-list>
      </div>

      <!-- 로그인되지 않은 상태 -->
      <div class="login-prompt" v-else>
        <ion-card>
          <ion-card-content class="text-center">
            <ion-icon :icon="personCircleOutline" size="large" color="medium"></ion-icon>
            <h2>로그인이 필요합니다</h2>
            <p>주문이요의 다양한 서비스를 이용하려면 로그인해주세요.</p>
            <ion-button expand="block" @click="goToLogin" class="login-button">
              로그인하기
            </ion-button>
          </ion-card-content>
        </ion-card>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="js">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { 
  personCircleOutline, 
  personOutline, 
  notificationsOutline, 
  helpCircleOutline, 
  logOutOutline,
  chevronForwardOutline 
} from 'ionicons/icons';
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonCard,
  IonCardContent,
  IonList,
  IonItem,
  IonLabel,
  IonIcon,
  IonChip,
  IonButton,
  IonSpinner,
  toastController,
  alertController
} from '@ionic/vue';

const router = useRouter();
const authStore = useAuthStore();

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
</script>

<style scoped>
.profile-container {
  padding: 16px;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-info-card ion-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  font-size: 64px;
  color: #ff1744;
}

.user-details h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.user-details p {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
}

.login-prompt {
  padding: 40px 16px;
}

.text-center {
  text-align: center;
}

.login-prompt ion-icon {
  font-size: 80px;
  margin-bottom: 16px;
}

.login-prompt h2 {
  margin: 16px 0 8px 0;
  color: #333;
}

.login-prompt p {
  margin: 0 0 24px 0;
  color: #666;
}

.login-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --background-hover: #e50032;
  --border-radius: 8px;
  --color: white;
  height: 48px;
  font-weight: 600;
}
</style>
