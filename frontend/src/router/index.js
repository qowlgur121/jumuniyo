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
      {
        path: 'tab4',
        component: () => import('@/views/Tab4Page.vue'),
      },
      {
        path: 'tab5',
        component: () => import('@/views/Tab5Page.vue'),
      },
    ],
  },
  {
    path: '/auth/signup',
    name: 'SignUp',
    component: () => import('@/views/auth/SignUpPage.vue'),
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
  },
  {
    path: '/auth/find-email',
    name: 'FindEmail',
    component: () => import('@/views/auth/FindEmailPage.vue'),
  },
  {
    path: '/auth/find-password',
    name: 'FindPassword',
    component: () => import('@/views/auth/FindPasswordPage.vue'),
  },
  {
    path: '/auth/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/auth/ResetPasswordPage.vue'),
  },
  {
    path: '/store/register',
    name: 'StoreRegister',
    component: () => import('@/views/store/StoreRegistrationPage.vue'),
    meta: { requiresAuth: true } // 인증 필요
  },
  {
    path: '/store/my',
    name: 'MyStores',
    component: () => import('@/views/store/MyStoresPage.vue'),
    meta: { requiresAuth: true } // 인증 필요
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 인증이 필요한 라우트 가드 (추후 구현)
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const isAuthenticated = localStorage.getItem('token'); // 간단한 토큰 체크

  if (requiresAuth && !isAuthenticated) {
    // 인증이 필요한 페이지인데 로그인하지 않은 경우
    next('/auth/login');
  } else {
    next();
  }
});

export default router;