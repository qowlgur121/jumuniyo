import apiClient from './api.js';

// 카테고리 관련 API
export const categoryApi = {
  // 활성화된 카테고리 목록 조회
  getActiveCategories: () => {
    return apiClient.get('/categories');
  },

  // 카테고리 상세 조회
  getCategoryById: (categoryId) => {
    return apiClient.get(`/categories/${categoryId}`);
  }
};

// 음식점 관련 API
export const storeApi = {
  // 음식점 등록
  createStore: (storeData) => {
    return apiClient.post('/stores', storeData);
  },

  // 음식점 상세 조회
  getStoreById: (storeId) => {
    return apiClient.get(`/stores/${storeId}`);
  },

  // 음식점 수정
  updateStore: (storeId, storeData) => {
    return apiClient.put(`/stores/${storeId}`, storeData);
  },

  // 음식점 삭제 (비활성화)
  deleteStore: (storeId) => {
    return apiClient.delete(`/stores/${storeId}`);
  },

  // 내 음식점 목록 조회
  getMyStores: (page = 0, size = 10) => {
    return apiClient.get('/stores/my', {
      params: { page, size }
    });
  },

  // 음식점 검색/목록 조회
  searchStores: (params = {}) => {
    const {
      keyword,
      categoryId,
      area,
      sortBy = 'rating',
      approvedOnly = false,
      page = 0,
      size = 20
    } = params;

    return apiClient.get('/stores', {
      params: {
        keyword,
        categoryId,
        area,
        sortBy,
        approvedOnly,
        page,
        size
      }
    });
  },

  // 음식점 승인 (관리자용)
  approveStore: (storeId) => {
    return apiClient.post(`/stores/${storeId}/approve`);
  },

  // 음식점 승인 거부 (관리자용)
  rejectStore: (storeId) => {
    return apiClient.post(`/stores/${storeId}/reject`);
  }
}; 