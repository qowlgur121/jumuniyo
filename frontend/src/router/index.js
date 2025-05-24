// jumuniyo/frontend/src/router/index.js

import { createRouter, createWebHistory } from '@ionic/vue-router';
// import { RouteRecordRaw } from 'vue-router'; // JavaScript 파일이므로 이 타입 임포트 제거
import TabsPage from '../views/TabsPage.vue';

// JavaScript 배열로 routes 정의
const routes = [
  {
    path: '/',
    redirect: '/tabs/tab1',
  },
  {
    path: '/tabs/',
    component: TabsPage,
    children: [
      {
        path: '',
        redirect: '/tabs/tab1',
      },
      {
        path: 'tab1',
        component: () => import('@/views/Tab1Page.vue'),
      },
      {
        path: 'tab2',
        component: () => import('@/views/Tab2Page.vue'),
      },
      {
        path: 'tab3',
        component: () => import('@/views/Tab3Page.vue'),
      },
    ],
  },
  {
    path: '/auth/signup',
    name: 'SignUp',
    component: () => import('@/views/auth/SignUpPage.vue'),
    meta: {
      // hideTabs: true, // 필요시 App.vue에서 이 meta 정보를 활용하여 탭 숨김 처리
    },
  },
  // TODO: 로그인 페이지 라우트 추가 예정 (/auth/login)
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;