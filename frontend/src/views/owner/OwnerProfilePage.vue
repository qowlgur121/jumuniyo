<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title>프로필 설정</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <div class="responsive-container">
        <div class="page-header">
          <h1>프로필 설정</h1>
          <p>계정 정보와 설정을 변경하세요</p>
        </div>

        <div class="coming-soon">
          <h2>👤 프로필 설정 기능</h2>
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