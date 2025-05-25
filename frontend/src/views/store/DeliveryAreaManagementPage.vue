<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>배달지역 관리</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="saveAllDeliveryAreas" :disabled="isLoading">
            <ion-icon :icon="saveOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="delivery-area-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="management-header">
          <h1 class="responsive-title">배달지역 설정</h1>
          <p class="subtitle">음식점의 배달 가능 지역을 지도에서 설정해주세요</p>
        </div>

        <!-- 음식점 선택 -->
        <div class="form-section" v-if="stores.length > 1">
          <h2 class="section-title">음식점 선택</h2>
          <ion-item class="form-item">
            <ion-select
              v-model="selectedStoreId"
              placeholder="음식점을 선택하세요"
              interface="action-sheet"
              @ionChange="onStoreChange"
            >
              <div slot="label">음식점 <span class="required">*</span></div>
              <ion-select-option
                v-for="store in stores"
                :key="store.id"
                :value="store.id"
              >
                {{ store.name }}
              </ion-select-option>
            </ion-select>
          </ion-item>
        </div>

        <!-- 지도 섹션 -->
        <div class="form-section" v-if="selectedStoreId">
          <h2 class="section-title">
            <ion-icon :icon="mapOutline"></ion-icon>
            배달지역 지도
          </h2>
          <div class="map-container">
            <div id="kakao-map" class="kakao-map"></div>
            <div class="map-controls">
              <ion-button 
                fill="outline" 
                size="small" 
                @click="addNewArea"
                class="control-button"
              >
                <ion-icon :icon="addOutline" slot="start"></ion-icon>
                지역 추가
              </ion-button>
              <ion-button 
                fill="outline" 
                size="small" 
                @click="clearAllAreas"
                class="control-button"
                color="danger"
              >
                <ion-icon :icon="trashOutline" slot="start"></ion-icon>
                전체 삭제
              </ion-button>
            </div>
          </div>
        </div>

        <!-- 배달지역 목록 -->
        <div class="form-section" v-if="selectedStoreId && deliveryAreas.length > 0">
          <h2 class="section-title">
            <ion-icon :icon="listOutline"></ion-icon>
            배달지역 목록
          </h2>
          <div class="area-list">
            <div 
              v-for="(area, index) in deliveryAreas" 
              :key="area.id || index"
              class="area-card"
              :class="{ 'area-card-selected': selectedAreaIndex === index }"
              @click="selectArea(index)"
            >
              <!-- 지역 헤더 -->
              <div class="area-header">
                <div class="area-info">
                  <h3 class="area-name">{{ area.areaName }}</h3>
                  <p class="area-detail" v-if="area.detailAddress">{{ area.detailAddress }}</p>
                </div>
                <div class="area-actions">
                  <ion-button 
                    fill="clear" 
                    size="small" 
                    @click.stop="editArea(index)"
                    class="action-button"
                  >
                    <ion-icon :icon="pencilOutline"></ion-icon>
                  </ion-button>
                  <ion-button 
                    fill="clear" 
                    size="small" 
                    @click.stop="deleteArea(index)"
                    class="action-button"
                    color="danger"
                  >
                    <ion-icon :icon="trashOutline"></ion-icon>
                  </ion-button>
                </div>
              </div>

              <!-- 지역 상세 정보 -->
              <div class="area-details">
                <div class="detail-row">
                  <span class="label">배달비:</span>
                  <span class="value">{{ formatCurrency(area.deliveryFee) }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">최소 주문:</span>
                  <span class="value">{{ formatCurrency(area.minimumOrderAmount) }}</span>
                </div>
                <div class="detail-row">
                  <span class="label">배달 시간:</span>
                  <span class="value">{{ area.estimatedDeliveryTime }}분</span>
                </div>
                <div class="detail-row">
                  <span class="label">상태:</span>
                  <span class="value" :class="{ 'status-active': area.isActive, 'status-inactive': !area.isActive }">
                    {{ area.isActive ? '활성' : '비활성' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 저장 버튼 -->
        <div class="submit-section" v-if="selectedStoreId">
          <ion-button
            expand="block"
            size="large"
            class="submit-button"
            @click="saveAllDeliveryAreas"
            :disabled="isLoading"
          >
            <ion-spinner v-if="isLoading" name="crescent"></ion-spinner>
            <span v-else>배달지역 저장</span>
          </ion-button>
        </div>

        <!-- 빈 상태 (음식점 선택되지 않음) -->
        <div v-if="!selectedStoreId && stores.length > 0" class="empty-state">
          <ion-icon :icon="storefront" class="empty-icon"></ion-icon>
          <h3>음식점을 선택해주세요</h3>
          <p>배달지역을 설정할 음식점을 선택하세요.</p>
        </div>

        <!-- 빈 상태 (등록된 음식점 없음) -->
        <div v-if="stores.length === 0" class="empty-state">
          <ion-icon :icon="addCircleOutline" class="empty-icon"></ion-icon>
          <h3>등록된 음식점이 없습니다</h3>
          <p>먼저 음식점을 등록해주세요.</p>
          <ion-button 
            fill="outline" 
            @click="$router.push('/store/register')"
            class="action-button"
          >
            음식점 등록하기
          </ion-button>
        </div>
      </div>
    </ion-content>

    <!-- 배달지역 추가/수정 모달 -->
    <ion-modal :is-open="isAreaModalOpen" @willDismiss="closeAreaModal">
      <ion-header>
        <ion-toolbar>
          <ion-title>{{ editingAreaIndex >= 0 ? '배달지역 수정' : '배달지역 추가' }}</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeAreaModal">닫기</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content class="modal-content">
        <div class="modal-form">
          <ion-item class="form-item">
            <ion-input
              v-model="areaForm.areaName"
              placeholder="배달지역명을 입력하세요"
              :clear-input="true"
            ></ion-input>
            <div slot="label">지역명 <span class="required">*</span></div>
          </ion-item>

          <ion-item class="form-item">
            <ion-input
              v-model="areaForm.detailAddress"
              placeholder="상세 주소를 입력하세요 (선택사항)"
              :clear-input="true"
            ></ion-input>
            <div slot="label">상세 주소</div>
          </ion-item>

          <ion-item class="form-item">
            <ion-input
              type="number"
              v-model.number="areaForm.deliveryFee"
              placeholder="0"
              :clear-input="true"
            ></ion-input>
            <div slot="label">배달비 (원) <span class="required">*</span></div>
          </ion-item>

          <ion-item class="form-item">
            <ion-input
              type="number"
              v-model.number="areaForm.minimumOrderAmount"
              placeholder="0"
              :clear-input="true"
            ></ion-input>
            <div slot="label">최소 주문 금액 (원) <span class="required">*</span></div>
          </ion-item>

          <ion-item class="form-item">
            <ion-input
              type="number"
              v-model.number="areaForm.estimatedDeliveryTime"
              placeholder="30"
              :clear-input="true"
            ></ion-input>
            <div slot="label">예상 배달 시간 (분) <span class="required">*</span></div>
          </ion-item>

          <ion-item class="form-item">
            <ion-toggle v-model="areaForm.isActive"></ion-toggle>
            <div slot="label">활성화</div>
          </ion-item>

          <div class="modal-actions">
            <ion-button 
              expand="block" 
              @click="saveArea"
              :disabled="!isAreaFormValid"
              class="save-button"
            >
              {{ editingAreaIndex >= 0 ? '수정' : '추가' }}
            </ion-button>
          </div>
        </div>
      </ion-content>
    </ion-modal>

    <!-- 로딩 오버레이 -->
    <ion-loading
      :is-open="isLoading"
      message="저장 중..."
    ></ion-loading>

    <!-- 토스트 -->
    <ion-toast
      :is-open="showToast"
      :message="toastMessage"
      :duration="3000"
      @didDismiss="showToast = false"
    ></ion-toast>
  </ion-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  IonPage,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonContent,
  IonButtons,
  IonBackButton,
  IonButton,
  IonIcon,
  IonItem,
  IonInput,
  IonSelect,
  IonSelectOption,
  IonToggle,
  IonModal,
  IonSpinner,
  IonLoading,
  IonToast,
  alertController
} from '@ionic/vue'
import {
  saveOutline,
  mapOutline,
  addOutline,
  trashOutline,
  listOutline,
  pencilOutline,
  storefront,
  addCircleOutline
} from 'ionicons/icons'
import axios from 'axios'

// 카카오맵 타입 정의
declare global {
  interface Window {
    kakao: any
  }
}

// 라우터
const router = useRouter()

// 상태 관리
const isLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const selectedStoreId = ref<number | null>(null)
const stores = ref<any[]>([])
const deliveryAreas = ref<any[]>([])
const selectedAreaIndex = ref(-1)
const isAreaModalOpen = ref(false)
const editingAreaIndex = ref(-1)

// 지도 관련
let kakaoMap: any = null
let kakaoMarkers: any[] = []
let kakaoCircles: any[] = []

// 배달지역 폼
const areaForm = reactive({
  areaName: '',
  detailAddress: '',
  deliveryFee: 0,
  minimumOrderAmount: 0,
  estimatedDeliveryTime: 30,
  isActive: true
})

// 컴포넌트 마운트 시
onMounted(async () => {
  await loadStores()
  if (stores.value.length === 1) {
    selectedStoreId.value = stores.value[0].id
    await loadDeliveryAreas()
  }
  await initKakaoMap()
})

// 컴포넌트 언마운트 시
onUnmounted(() => {
  if (kakaoMap) {
    kakaoMap = null
  }
  kakaoMarkers = []
  kakaoCircles = []
})

// 음식점 변경 감지
watch(selectedStoreId, async (newStoreId) => {
  if (newStoreId) {
    await loadDeliveryAreas()
    updateMapMarkers()
  }
})

// 폼 유효성 검사
const isAreaFormValid = computed(() => {
  return areaForm.areaName.trim() !== '' && 
         areaForm.deliveryFee >= 0 && 
         areaForm.minimumOrderAmount >= 0 && 
         areaForm.estimatedDeliveryTime > 0
})

// 메소드들
const loadStores = async () => {
  try {
    // TODO: 실제 API 엔드포인트로 교체
    const response = await axios.get('/api/v1/stores/my-stores')
    stores.value = response.data
  } catch (error) {
    console.error('음식점 목록 로드 실패:', error)
    showToastMessage('음식점 목록을 불러오는데 실패했습니다.')
  }
}

const loadDeliveryAreas = async () => {
  if (!selectedStoreId.value) return
  
  try {
    const response = await axios.get(`/api/v1/stores/${selectedStoreId.value}/delivery-areas`)
    deliveryAreas.value = response.data || []
    updateMapMarkers()
  } catch (error) {
    console.error('배달지역 로드 실패:', error)
    showToastMessage('배달지역 정보를 불러오는데 실패했습니다.')
  }
}

const initKakaoMap = async () => {
  await nextTick()
  
  if (typeof window.kakao === 'undefined') {
    // 카카오맵 SDK 동적 로드
    const script = document.createElement('script')
    script.src = '//dapi.kakao.com/v2/maps/sdk.js?appkey=YOUR_APP_KEY&libraries=services'
    script.onload = () => initMapAfterLoad()
    document.head.appendChild(script)
  } else {
    initMapAfterLoad()
  }
}

const initMapAfterLoad = () => {
  const container = document.getElementById('kakao-map')
  if (!container || !window.kakao) return
  
  const options = {
    center: new window.kakao.maps.LatLng(37.5665, 126.9780), // 서울 시청
    level: 6
  }
  
  kakaoMap = new window.kakao.maps.Map(container, options)
  
  // 지도 클릭 이벤트
  window.kakao.maps.event.addListener(kakaoMap, 'click', (mouseEvent: any) => {
    const latlng = mouseEvent.latLng
    onMapClick(latlng.getLat(), latlng.getLng())
  })
}

const onMapClick = (lat: number, lng: number) => {
  // 지도 클릭 시 새로운 배달지역 추가 모달 열기
  const geocoder = new window.kakao.maps.services.Geocoder()
  
  geocoder.coord2Address(lng, lat, (result: any, status: any) => {
    if (status === window.kakao.maps.services.Status.OK) {
      const address = result[0]?.road_address || result[0]?.address
      areaForm.areaName = address?.region_2depth_name || '새 배달지역'
      areaForm.detailAddress = address?.address_name || ''
    } else {
      areaForm.areaName = '새 배달지역'
      areaForm.detailAddress = ''
    }
    
    // 폼 초기화
    areaForm.deliveryFee = 3000
    areaForm.minimumOrderAmount = 15000
    areaForm.estimatedDeliveryTime = 30
    areaForm.isActive = true
    
    editingAreaIndex.value = -1
    isAreaModalOpen.value = true
  })
}

const updateMapMarkers = () => {
  if (!kakaoMap) return
  
  // 기존 마커와 원 제거
  kakaoMarkers.forEach(marker => marker.setMap(null))
  kakaoCircles.forEach(circle => circle.setMap(null))
  kakaoMarkers = []
  kakaoCircles = []
  
  // 새 마커 추가 (임시 좌표 사용)
  deliveryAreas.value.forEach((area, index) => {
    const lat = 37.5665 + (Math.random() - 0.5) * 0.1
    const lng = 126.9780 + (Math.random() - 0.5) * 0.1
    
    const marker = new window.kakao.maps.Marker({
      position: new window.kakao.maps.LatLng(lat, lng),
      map: kakaoMap
    })
    
    const infoWindow = new window.kakao.maps.InfoWindow({
      content: `<div style="padding:5px;">${area.areaName}<br/>배달비: ${formatCurrency(area.deliveryFee)}</div>`
    })
    
    window.kakao.maps.event.addListener(marker, 'click', () => {
      infoWindow.open(kakaoMap, marker)
      selectArea(index)
    })
    
    kakaoMarkers.push(marker)
  })
}

const onStoreChange = () => {
  loadDeliveryAreas()
}

const selectArea = (index: number) => {
  selectedAreaIndex.value = index
}

const addNewArea = () => {
  // 폼 초기화
  areaForm.areaName = ''
  areaForm.detailAddress = ''
  areaForm.deliveryFee = 3000
  areaForm.minimumOrderAmount = 15000
  areaForm.estimatedDeliveryTime = 30
  areaForm.isActive = true
  
  editingAreaIndex.value = -1
  isAreaModalOpen.value = true
}

const editArea = (index: number) => {
  const area = deliveryAreas.value[index]
  if (area) {
    areaForm.areaName = area.areaName
    areaForm.detailAddress = area.detailAddress || ''
    areaForm.deliveryFee = area.deliveryFee
    areaForm.minimumOrderAmount = area.minimumOrderAmount
    areaForm.estimatedDeliveryTime = area.estimatedDeliveryTime
    areaForm.isActive = area.isActive
    
    editingAreaIndex.value = index
    isAreaModalOpen.value = true
  }
}

const deleteArea = async (index: number) => {
  const alert = await alertController.create({
    header: '배달지역 삭제',
    message: '이 배달지역을 삭제하시겠습니까?',
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '삭제',
        handler: () => {
          deliveryAreas.value.splice(index, 1)
          updateMapMarkers()
          showToastMessage('배달지역이 삭제되었습니다.')
        }
      }
    ]
  })
  await alert.present()
}

const clearAllAreas = async () => {
  const alert = await alertController.create({
    header: '전체 삭제',
    message: '모든 배달지역을 삭제하시겠습니까?',
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '삭제',
        handler: () => {
          deliveryAreas.value = []
          updateMapMarkers()
          showToastMessage('모든 배달지역이 삭제되었습니다.')
        }
      }
    ]
  })
  await alert.present()
}

const closeAreaModal = () => {
  isAreaModalOpen.value = false
  editingAreaIndex.value = -1
}

const saveArea = () => {
  if (!isAreaFormValid.value) return
  
  const newArea = {
    id: editingAreaIndex.value >= 0 ? deliveryAreas.value[editingAreaIndex.value].id : Date.now(),
    areaName: areaForm.areaName,
    detailAddress: areaForm.detailAddress,
    deliveryFee: areaForm.deliveryFee,
    minimumOrderAmount: areaForm.minimumOrderAmount,
    estimatedDeliveryTime: areaForm.estimatedDeliveryTime,
    isActive: areaForm.isActive
  }
  
  if (editingAreaIndex.value >= 0) {
    // 수정
    deliveryAreas.value[editingAreaIndex.value] = newArea
    showToastMessage('배달지역이 수정되었습니다.')
  } else {
    // 추가
    deliveryAreas.value.push(newArea)
    showToastMessage('배달지역이 추가되었습니다.')
  }
  
  updateMapMarkers()
  closeAreaModal()
}

const saveAllDeliveryAreas = async () => {
  if (!selectedStoreId.value) {
    showToastMessage('음식점을 선택해주세요.')
    return
  }
  
  if (deliveryAreas.value.length === 0) {
    showToastMessage('저장할 배달지역이 없습니다.')
    return
  }

  isLoading.value = true
  
  try {
    // 일괄 저장 API 호출
    await axios.post(`/api/v1/stores/${selectedStoreId.value}/delivery-areas/batch`, deliveryAreas.value)
    
    showToastMessage('배달지역이 성공적으로 저장되었습니다!')
    
    // 저장 후 데이터 다시 로드
    await loadDeliveryAreas()
  } catch (error) {
    console.error('배달지역 저장 실패:', error)
    showToastMessage('배달지역 저장에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(amount)
}

const showToastMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}
</script>

<style scoped>
.delivery-area-content {
  --background: #f8f9fa;
}

.responsive-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.management-header {
  text-align: center;
  margin-bottom: 30px;
}

.responsive-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666;
  font-size: 1rem;
  margin: 0;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-container {
  position: relative;
}

.kakao-map {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  border: 2px solid #e9ecef;
}

.map-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 10px;
  z-index: 1000;
}

.control-button {
  --background: rgba(255, 255, 255, 0.9);
  --color: #333;
  backdrop-filter: blur(5px);
}

.area-list {
  display: grid;
  gap: 15px;
}

.area-card {
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
}

.area-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.15);
}

.area-card-selected {
  border-color: #007bff;
  background: #f8f9ff;
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.area-info h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 5px 0;
}

.area-detail {
  font-size: 0.9rem;
  color: #666;
  margin: 0;
}

.area-actions {
  display: flex;
  gap: 5px;
}

.action-button {
  --padding-start: 8px;
  --padding-end: 8px;
}

.area-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
}

.label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
}

.value {
  font-size: 0.9rem;
  color: #1a1a1a;
  font-weight: 600;
}

.status-active {
  color: #28a745;
}

.status-inactive {
  color: #dc3545;
}

.submit-section {
  margin-top: 30px;
}

.submit-button {
  --background: #007bff;
  --background-hover: #0056b3;
  --border-radius: 12px;
  height: 50px;
  font-weight: 600;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  color: #adb5bd;
}

.empty-state h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.empty-state p {
  margin: 0 0 20px 0;
}

.form-item {
  --background: #f8f9fa;
  --border-radius: 8px;
  margin-bottom: 15px;
}

.required {
  color: #dc3545;
}

.modal-content {
  --background: #f8f9fa;
}

.modal-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
}

.modal-actions {
  margin-top: 30px;
}

.save-button {
  --background: #007bff;
  --background-hover: #0056b3;
  --border-radius: 12px;
  height: 50px;
  font-weight: 600;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .responsive-container {
    padding: 15px;
  }
  
  .area-details {
    grid-template-columns: 1fr;
  }
  
  .map-controls {
    position: static;
    margin-top: 10px;
    justify-content: center;
  }
  
  .control-button {
    flex: 1;
  }
}
</style> 