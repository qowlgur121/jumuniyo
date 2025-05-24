<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title>주문 관리</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div class="responsive-container">
        <div class="page-header">
          <h1>주문 관리</h1>
          <p>실시간 주문을 확인하고 처리하세요</p>
        </div>

        <div class="coming-soon">
          <h2>📋 주문 관리 기능</h2>
          <p>곧 출시될 예정입니다!</p>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup>
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton
} from '@ionic/vue';
</script>

<style scoped>
.page-header {
  padding: 2rem 1rem;
  text-align: center;
  background: var(--yogiyo-primary);
  color: white;
}

.coming-soon {
  padding: 4rem 2rem;
  text-align: center;
}
</style> 