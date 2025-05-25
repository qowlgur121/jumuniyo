<template>
  <ion-page>
    <ion-tabs>
      <ion-router-outlet></ion-router-outlet>
      <ion-tab-bar slot="bottom">
        <ion-tab-button tab="tab1" href="/tabs/tab1">
          <ion-icon :icon="home"></ion-icon>
          <ion-label>홈</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="tab2" href="/tabs/tab2">
          <ion-icon :icon="receiptOutline"></ion-icon>
          <ion-label>주문내역</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="tab3" href="/tabs/tab3">
          <ion-icon :icon="heartOutline"></ion-icon>
          <ion-label>찜</ion-label>
        </ion-tab-button>

        <ion-tab-button tab="tab4" href="/tabs/tab4">
          <ion-icon :icon="person"></ion-icon>
          <ion-label>마이</ion-label>
        </ion-tab-button>
      </ion-tab-bar>
    </ion-tabs>
  </ion-page>
</template>

<script lang="ts">
import { IonPage, IonTabs, IonTabBar, IonTabButton, IonRouterOutlet, IonIcon, IonLabel } from '@ionic/vue';
import { home, receiptOutline, heartOutline, person } from 'ionicons/icons';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'TabsPage',
  components: {
    IonPage,
    IonTabs,
    IonTabBar,
    IonTabButton,
    IonRouterOutlet,
    IonIcon,
    IonLabel,
  },
  setup() {
    return {
      home,
      receiptOutline,
      heartOutline,
      person,
    };
  },
});
</script>

<style scoped>
/* 기본 탭바 스타일만 적용 */
ion-tab-bar {
  --background: #ffffff;
  --border: 1px solid #e9ecef;
  --color: #999999;
  --color-selected: #ff1744;
  height: 65px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

ion-tab-button {
  --color: #999999;
  --color-selected: #ff1744;
  --ripple-color: rgba(255, 23, 68, 0.2);
}

ion-tab-button ion-icon {
  font-size: 22px;
}

ion-tab-button ion-label {
  font-size: 11px;
  font-weight: 500;
}

/* 반응형 - 모바일 */
@media (max-width: 480px) {
  ion-tab-bar {
    height: 60px;
}

  ion-tab-button ion-icon {
    font-size: 20px;
}

  ion-tab-button ion-label {
    font-size: 10px;
  }
}

/* 반응형 - 태블릿 */
@media (min-width: 481px) and (max-width: 1023px) {
  ion-tab-bar {
    height: 70px;
  }
  
  ion-tab-button ion-icon {
    font-size: 24px;
  }
  
  ion-tab-button ion-label {
    font-size: 12px;
  }
}

/* 반응형 - 데스크톱 */
@media (min-width: 1024px) {
  ion-tab-bar {
    height: 75px;
  }
  
  ion-tab-button ion-icon {
    font-size: 26px;
  }
  
  ion-tab-button ion-label {
    font-size: 13px;
    font-weight: 600;
  }
}
</style>