import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import apiClient from '@/services/api';

export const useAuthStore = defineStore('auth', () => {
  // 상태
  const token = ref(localStorage.getItem('token') || null);
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'));
  const isLoading = ref(false);

  // Getters
  const isAuthenticated = computed(() => !!token.value);
  const userInfo = computed(() => user.value);

  // Actions
  const login = async (credentials) => {
    isLoading.value = true;
    try {
      const response = await apiClient.post('/auth/login', credentials);
      
      if (response.status === 200) {
        const { token: authToken, userId, email, nickname, role } = response.data;
        
        // 상태 업데이트
        token.value = authToken;
        user.value = { userId, email, nickname, role };
        
        // 로컬 스토리지에 저장
        localStorage.setItem('token', authToken);
        localStorage.setItem('user', JSON.stringify({ userId, email, nickname, role }));
        
        return { success: true };
      }
    } catch (error) {
      console.error('로그인 실패:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || '로그인 중 오류가 발생했습니다.' 
      };
    } finally {
      isLoading.value = false;
    }
  };

  const logout = async () => {
    isLoading.value = true;
    try {
      // 백엔드 로그아웃 API 호출
      await apiClient.post('/auth/logout');
    } catch (error) {
      console.error('로그아웃 API 오류:', error);
      // 백엔드 오류가 있어도 클라이언트 측 로그아웃은 진행
    } finally {
      // 클라이언트 측 상태 정리
      token.value = null;
      user.value = null;
      
      // 로컬 스토리지에서 제거
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      isLoading.value = false;
    }
  };

  const signup = async (userData) => {
    isLoading.value = true;
    try {
      const response = await apiClient.post('/auth/signup', userData);
      
      if (response.status === 201) {
        return { success: true };
      }
    } catch (error) {
      console.error('회원가입 실패:', error);
      return { 
        success: false, 
        error: error.response?.data?.message || '회원가입 중 오류가 발생했습니다.' 
      };
    } finally {
      isLoading.value = false;
    }
  };

  const initializeAuth = () => {
    // 앱 시작 시 로컬 스토리지에서 인증 정보 복원
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      token.value = storedToken;
      user.value = JSON.parse(storedUser);
    }
  };

  return {
    // 상태
    token,
    user,
    isLoading,
    
    // Getters
    isAuthenticated,
    userInfo,
    
    // Actions
    login,
    logout,
    signup,
    initializeAuth
  };
}); 