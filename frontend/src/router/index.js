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
  // 일반 회원 인증
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
  // 사장님 인증
  {
    path: '/owner/signup',
    name: 'OwnerSignUp',
    component: () => import('@/views/auth/OwnerSignUpPage.vue'),
  },
  {
    path: '/owner/login',
    name: 'OwnerLogin',
    component: () => import('@/views/auth/OwnerLoginPage.vue'),
  },
  // 사장님 전용 페이지들
  {
    path: '/owner/dashboard',
    name: 'OwnerDashboard',
    component: () => import('@/views/owner/OwnerDashboardPage.vue'),
    meta: { requiresAuth: true, requiresOwner: true }
  },

  {
    path: '/owner/orders',
    name: 'OwnerOrders',
    component: () => import('@/views/owner/OwnerOrdersPage.vue'),
    meta: { requiresAuth: true, requiresOwner: true }
  },
  {
    path: '/owner/analytics',
    name: 'OwnerAnalytics',
    component: () => import('@/views/owner/OwnerAnalyticsPage.vue'),
    meta: { requiresAuth: true, requiresOwner: true }
  },
  {
    path: '/owner/profile',
    name: 'OwnerProfile',
    component: () => import('@/views/owner/OwnerProfilePage.vue'),
    meta: { requiresAuth: true, requiresOwner: true }
  },
  // 일반 고객 음식점 관련 (기존)
  {
    path: '/store/register',
    name: 'StoreRegister',
    component: () => import('@/views/store/StoreRegistrationPage.vue'),
    meta: { requiresAuth: true } // 인증 필요
  },
  {
    path: '/store/edit/:id',
    name: 'StoreEdit',
    component: () => import('@/views/store/StoreEditPage.vue'),
    meta: { requiresAuth: true } // 인증 필요
  },

];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 역할 기반 인증 가드
router.beforeEach((to, from, next) => {
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiresOwner = to.matched.some(record => record.meta.requiresOwner);
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
  if (requiresAuth && !token) {
    // 인증이 필요한 페이지인데 로그인하지 않은 경우
    next('/auth/login');
    return;
  }

  if (requiresOwner && token) {
    // 사장님 전용 페이지인 경우 역할 확인
    try {
      const user = JSON.parse(userStr || '{}');
      if (user.role !== 'ROLE_OWNER') {
        // 사장님이 아닌 경우 사장님 로그인 페이지로 리다이렉트
        next('/owner/login');
        return;
      }
      
      if (user.status === 'PENDING_APPROVAL') {
        // 승인 대기 중인 사장님은 접근 제한
        next('/owner/login');
        return;
      }
    } catch (error) {
      console.error('사용자 정보 파싱 오류:', error);
      next('/owner/login');
      return;
    }
  }

  next();
});

export default router;