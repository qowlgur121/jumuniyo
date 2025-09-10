import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types/cart'

export const useCartStore = defineStore('cart', () => {
  // 장바구니 아이템
  const items = ref<CartItem[]>([])
  
  // 장바구니에 담긴 매장 ID
  const storeId = ref<number | null>(null)
  
  // 총 가격 계산
  const totalPrice = computed(() => {
    return items.value.reduce((sum, item) => sum + item.totalPrice, 0)
  })
  
  // 총 상품 개수
  const totalItems = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })
  
  // 장바구니가 비어있는지 확인
  const isEmpty = computed(() => items.value.length === 0)
  
  // 장바구니에 아이템 추가
  function addItem(item: CartItem) {
    // 다른 매장의 상품이 이미 담겨있는 경우
    if (storeId.value !== null && storeId.value !== item.storeId) {
      if (!confirm('다른 매장의 메뉴가 장바구니에 있습니다. 장바구니를 비우고 새로운 메뉴를 담으시겠습니까?')) {
        return false
      }
      // 장바구니 비우기
      clearCart()
    }
    
    // 매장 ID 설정
    storeId.value = item.storeId
    
    // 이미 있는 상품인지 확인 (ID와 옵션이 동일한 경우)
    const existingItemIndex = items.value.findIndex(
      i => i.id === item.id && i.selectedOptions === item.selectedOptions
    )
    
    if (existingItemIndex !== -1) {
      // 이미 있는 상품이면 수량만 증가
      items.value[existingItemIndex].quantity += item.quantity
      items.value[existingItemIndex].totalPrice += item.totalPrice
    } else {
      // 없는 상품이면 추가
      items.value.push(item)
    }
    
    // 로컬 스토리지에 저장
    saveCart()
    return true
  }
  
  // 장바구니에서 아이템 제거
  function removeItem(index: number) {
    items.value.splice(index, 1)
    
    // 장바구니가 비어있으면 매장 ID도 초기화
    if (items.value.length === 0) {
      storeId.value = null
    }
    
    // 로컬 스토리지에 저장
    saveCart()
  }
  
  // 장바구니 아이템 수량 변경
  function updateItemQuantity(index: number, quantity: number) {
    if (quantity <= 0) {
      // 수량이 0 이하면 삭제
      removeItem(index)
      return
    }
    
    const item = items.value[index]
    const pricePerItem = item.totalPrice / item.quantity
    
    // 수량과 총 가격 업데이트
    item.quantity = quantity
    item.totalPrice = pricePerItem * quantity
    
    // 로컬 스토리지에 저장
    saveCart()
  }
  
  // 장바구니 비우기
  function clearCart() {
    items.value = []
    storeId.value = null
    
    // 로컬 스토리지에서 삭제
    localStorage.removeItem('cart')
  }
  
  // 장바구니 저장
  function saveCart() {
    localStorage.setItem('cart', JSON.stringify({
      items: items.value,
      storeId: storeId.value
    }))
  }
  
  // 장바구니 불러오기
  function loadCart() {
    const stored = localStorage.getItem('cart')
    if (stored) {
      try {
        const data = JSON.parse(stored)
        items.value = data.items || []
        storeId.value = data.storeId || null
      } catch (e) {
        console.error('장바구니 불러오기 실패:', e)
        clearCart()
      }
    }
  }
  
  // 초기화
  function init() {
    loadCart()
  }
  
  // 스토어 초기화
  init()
  
  return {
    items,
    storeId,
    totalPrice,
    totalItems,
    isEmpty,
    addItem,
    removeItem,
    updateItemQuantity,
    clearCart
  }
}) 