import apiClient from './api.js';

// 로컬 캐시를 위한 객체
const cache = {
  categories: {
    data: null,
    timestamp: null,
    ttl: 60 * 60 * 1000 // 1시간
  },
  stores: {
    data: {},
    timestamp: {},
    ttl: 5 * 60 * 1000 // 5분
  },
  storeDetails: {
    data: {},
    timestamp: {},
    ttl: 10 * 60 * 1000 // 10분
  }
};

// 캐시 유효성 검사
const isCacheValid = (cacheEntry) => {
  return cacheEntry.data !== null && 
         cacheEntry.timestamp !== null && 
         (Date.now() - cacheEntry.timestamp) < cacheEntry.ttl;
};

// 캐시 키 생성
const createCacheKey = (params) => {
  return Object.entries(params)
    .filter(([_, value]) => value !== undefined && value !== null)
    .sort(([keyA], [keyB]) => keyA.localeCompare(keyB))
    .map(([key, value]) => `${key}:${value}`)
    .join('|');
};

// 카테고리 관련 API
export const categoryApi = {
  // 활성화된 카테고리 목록 조회
  getActiveCategories: async () => {
    // 캐시 확인
    if (isCacheValid(cache.categories)) {
      console.log('Using cached categories');
      return { data: cache.categories.data };
    }

    const response = await apiClient.get('/categories');
    
    // 캐시 업데이트
    if (response.data) {
      cache.categories.data = response.data;
      cache.categories.timestamp = Date.now();
    }
    
    return response;
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

  // 음식점 상세 조회 (캐싱 적용)
  getStoreById: async (storeId) => {
    const cacheKey = `store:${storeId}`;
    
    // 캐시 확인
    if (cache.storeDetails.data[cacheKey] && 
        (Date.now() - cache.storeDetails.timestamp[cacheKey]) < cache.storeDetails.ttl) {
      console.log(`Using cached store details for ID: ${storeId}`);
      return { data: cache.storeDetails.data[cacheKey] };
    }

    const response = await apiClient.get(`/stores/${storeId}`);
    
    // 캐시 업데이트
    if (response.data) {
      cache.storeDetails.data[cacheKey] = response.data;
      cache.storeDetails.timestamp[cacheKey] = Date.now();
    }
    
    return response;
  },

  // 음식점 수정 (캐시 무효화)
  updateStore: async (storeId, storeData) => {
    // 관련 캐시 삭제
    const cacheKey = `store:${storeId}`;
    delete cache.storeDetails.data[cacheKey];
    delete cache.storeDetails.timestamp[cacheKey];
    
    // 목록 캐시도 초기화
    cache.stores.data = {};
    cache.stores.timestamp = {};
    
    return apiClient.put(`/stores/${storeId}`, storeData);
  },

  // 음식점 삭제 (캐시 무효화)
  deleteStore: async (storeId) => {
    // 관련 캐시 삭제
    const cacheKey = `store:${storeId}`;
    delete cache.storeDetails.data[cacheKey];
    delete cache.storeDetails.timestamp[cacheKey];
    
    // 목록 캐시도 초기화
    cache.stores.data = {};
    cache.stores.timestamp = {};
    
    return apiClient.delete(`/stores/${storeId}`);
  },

  // 내 음식점 목록 조회
  getMyStores: (page = 0, size = 10) => {
    return apiClient.get('/stores/my', {
      params: { page, size }
    });
  },

  // 음식점 검색/목록 조회 (캐싱 적용)
  searchStores: async (params = {}) => {
    const {
      keyword,
      categoryId,
      area,
      sortBy = 'rating',
      approvedOnly = false,
      // 필터링 파라미터 추가
      minRating,
      maxMinimumOrderAmount,
      maxDeliveryFee,
      maxDeliveryTime,
      minReviewCount,
      freeDeliveryOnly = false,
      newStoreOnly = false,
      page = 0,
      size = 20
    } = params;

    const requestParams = {
      keyword,
      categoryId,
      area,
      sortBy,
      approvedOnly,
      minRating,
      maxMinimumOrderAmount,
      maxDeliveryFee,
      maxDeliveryTime,
      minReviewCount,
      freeDeliveryOnly,
      newStoreOnly,
      page,
      size
    };

    const cacheKey = createCacheKey(requestParams);
    
    // 캐시 확인
    if (cache.stores.data[cacheKey] && 
        (Date.now() - cache.stores.timestamp[cacheKey]) < cache.stores.ttl) {
      console.log('Using cached store search results');
      return { data: cache.stores.data[cacheKey] };
    }

    const response = await apiClient.get('/stores', { params: requestParams });
    
    // 캐시 업데이트
    if (response.data) {
      cache.stores.data[cacheKey] = response.data;
      cache.stores.timestamp[cacheKey] = Date.now();
    }
    
    return response;
  },

  // 위치 기반 음식점 검색 (최적화된 API 사용)
  searchStoresWithLocation: async (params = {}) => {
    const {
      latitude,
      longitude,
      radiusKm,
      keyword,
      categoryId,
      area,
      sortBy = 'distance',
      approvedOnly = false,
      // 필터링 파라미터 추가
      minRating,
      maxMinimumOrderAmount,
      maxDeliveryFee,
      maxDeliveryTime,
      minReviewCount,
      freeDeliveryOnly = false,
      newStoreOnly = false,
      page = 0,
      size = 20
    } = params;

    // 위치 정보가 없으면 일반 검색으로 폴백
    if (!latitude || !longitude) {
      console.warn('Location data missing, falling back to regular search');
      return storeApi.searchStores(params);
    }

    const requestParams = {
      latitude,
      longitude,
      radiusKm,
      keyword,
      categoryId,
      area,
      sortBy,
      approvedOnly,
      minRating,
      maxMinimumOrderAmount,
      maxDeliveryFee,
      maxDeliveryTime,
      minReviewCount,
      freeDeliveryOnly,
      newStoreOnly,
      page,
      size
    };

    const cacheKey = createCacheKey(requestParams);
    
    // 캐시 확인 (위치 기반 검색은 TTL을 짧게 설정)
    const locationCacheTTL = 3 * 60 * 1000; // 3분
    if (cache.stores.data[cacheKey] && 
        (Date.now() - cache.stores.timestamp[cacheKey]) < locationCacheTTL) {
      console.log('Using cached location-based store results');
      return { data: cache.stores.data[cacheKey] };
    }

    // 최적화된 위치 기반 검색 API 사용
    const response = await apiClient.get('/stores/location/optimized', {
      params: requestParams
    });
    
    // 캐시 업데이트
    if (response.data) {
      cache.stores.data[cacheKey] = response.data;
      cache.stores.timestamp[cacheKey] = Date.now();
    }
    
    return response;
  },

  // 음식점 승인 (관리자용)
  approveStore: (storeId) => {
    // 캐시 무효화
    cache.stores.data = {};
    cache.stores.timestamp = {};
    
    return apiClient.post(`/stores/${storeId}/approve`);
  },

  // 음식점 승인 거부 (관리자용)
  rejectStore: (storeId) => {
    // 캐시 무효화
    cache.stores.data = {};
    cache.stores.timestamp = {};
    
    return apiClient.post(`/stores/${storeId}/reject`);
  },
  
  // 캐시 수동 초기화
  clearCache: () => {
    cache.categories.data = null;
    cache.categories.timestamp = null;
    cache.stores.data = {};
    cache.stores.timestamp = {};
    cache.storeDetails.data = {};
    cache.storeDetails.timestamp = {};
    console.log('Store API cache cleared');
  }
}; 