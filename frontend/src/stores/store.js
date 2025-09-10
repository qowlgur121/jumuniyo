import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storeApi, publicStoreApi, cacheUtils } from '@/services/storeApi.js'

export const useStoreStore = defineStore('store', () => {
  // 상태
  const currentStore = ref(null)
  const recentStores = ref([])
  const favoriteStores = ref([])
  const storeList = ref([])
  const loading = ref(false)
  const error = ref(null)
  
  // 검색 및 필터링 상태
  const searchFilters = ref({
    keyword: '',
    categoryId: null,
    area: '',
    sortBy: 'rating',
    minRating: null,
    maxDeliveryFee: null,
    freeDeliveryOnly: false
  })
  
  // 계산된 속성
  const filteredStores = computed(() => {
    if (!storeList.value.length) return []
    
    return storeList.value.filter(store => {
      // 키워드 필터링
      if (searchFilters.value.keyword) {
        const keyword = searchFilters.value.keyword.toLowerCase()
        if (!store.name.toLowerCase().includes(keyword) &&
            !store.description?.toLowerCase().includes(keyword)) {
          return false
        }
      }
      
      // 카테고리 필터링
      if (searchFilters.value.categoryId && 
          store.category?.id !== searchFilters.value.categoryId) {
        return false
      }
      
      // 최소 평점 필터링
      if (searchFilters.value.minRating && 
          store.rating < searchFilters.value.minRating) {
        return false
      }
      
      // 최대 배달비 필터링
      if (searchFilters.value.maxDeliveryFee && 
          store.deliveryFee > searchFilters.value.maxDeliveryFee) {
        return false
      }
      
      // 무료배달만 보기
      if (searchFilters.value.freeDeliveryOnly && store.deliveryFee > 0) {
        return false
      }
      
      return true
    })
  })
  
  // Actions
  
  // 매장 검색
  async function searchStores(params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const response = await storeApi.searchStores({
        ...searchFilters.value,
        ...params
      })
      
      storeList.value = response.data.content || response.data
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 공개 매장 검색
  async function searchPublicStores(params = {}) {
    loading.value = true
    error.value = null
    
    try {
      const response = await publicStoreApi.searchPublicStores({
        ...searchFilters.value,
        ...params,
        approvedOnly: true
      })
      
      storeList.value = response.data.content || response.data
      return response.data
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 매장 상세 조회
  async function getStoreById(storeId) {
    loading.value = true
    error.value = null
    
    try {
      const response = await storeApi.getStoreById(storeId)
      const store = response.data
      
      if (store) {
        addToRecentStores(store)
      }
      
      return store
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 공개 매장 상세 조회
  async function getPublicStoreById(storeId) {
    loading.value = true
    error.value = null
    
    try {
      const response = await publicStoreApi.getPublicStoreById(storeId)
      const store = response.data
      
      if (store) {
        addToRecentStores(store)
      }
      
      return store
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }
  
  // 현재 매장 설정
  function setCurrentStore(store) {
    currentStore.value = store
    addToRecentStores(store)
  }
  
  // 최근 본 매장 목록에 추가
  function addToRecentStores(store) {
    const index = recentStores.value.findIndex(s => s.id === store.id)
    if (index !== -1) {
      recentStores.value.splice(index, 1)
    }
    
    recentStores.value.unshift(store)
    
    if (recentStores.value.length > 10) {
      recentStores.value.pop()
    }
    
    saveRecentStores()
  }
  
  // 즐겨찾기 토글
  function toggleFavorite(store) {
    const index = favoriteStores.value.findIndex(s => s.id === store.id)
    if (index !== -1) {
      favoriteStores.value.splice(index, 1)
    } else {
      favoriteStores.value.push(store)
    }
    
    saveFavoriteStores()
  }
  
  // 검색 필터 업데이트
  function updateFilters(filters) {
    searchFilters.value = { ...searchFilters.value, ...filters }
  }
  
  // 검색 필터 초기화
  function resetFilters() {
    searchFilters.value = {
      keyword: '',
      categoryId: null,
      area: '',
      sortBy: 'rating',
      minRating: null,
      maxDeliveryFee: null,
      freeDeliveryOnly: false
    }
  }
  
  // 로컬 스토리지 관리
  function saveRecentStores() {
    localStorage.setItem('recentStores', JSON.stringify(recentStores.value))
  }
  
  function loadRecentStores() {
    const stored = localStorage.getItem('recentStores')
    if (stored) {
      try {
        recentStores.value = JSON.parse(stored)
      } catch (e) {
        console.error('최근 본 매장 목록 불러오기 실패:', e)
        recentStores.value = []
      }
    }
  }
  
  function saveFavoriteStores() {
    localStorage.setItem('favoriteStores', JSON.stringify(favoriteStores.value))
  }
  
  function loadFavoriteStores() {
    const stored = localStorage.getItem('favoriteStores')
    if (stored) {
      try {
        favoriteStores.value = JSON.parse(stored)
      } catch (e) {
        console.error('즐겨찾기 매장 목록 불러오기 실패:', e)
        favoriteStores.value = []
      }
    }
  }
  
  // 캐시 관리
  function clearCache() {
    cacheUtils.clearAll()
  }
  
  // 초기화
  function init() {
    loadRecentStores()
    loadFavoriteStores()
  }
  
  // 스토어 초기화
  init()
  
  return {
    // 상태
    currentStore,
    recentStores,
    favoriteStores,
    storeList,
    loading,
    error,
    searchFilters,
    
    // 계산된 속성
    filteredStores,
    
    // 액션
    searchStores,
    searchPublicStores,
    getStoreById,
    getPublicStoreById,
    setCurrentStore,
    addToRecentStores,
    toggleFavorite,
    updateFilters,
    resetFilters,
    clearCache
  }
}) 