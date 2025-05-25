<template>
  <ion-page>
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/"></ion-back-button>
        </ion-buttons>
        <ion-title>운영시간 관리</ion-title>
        <ion-buttons slot="end">
          <ion-button fill="clear" @click="saveAllOperatingHours" :disabled="isLoading">
            <ion-icon :icon="saveOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content class="operating-hour-content">
      <div class="responsive-container">
        <!-- 헤더 섹션 -->
        <div class="management-header">
          <h1 class="responsive-title">음식점 운영시간 설정</h1>
          <p class="subtitle">고객들이 주문 가능한 시간을 설정해주세요</p>
        </div>

        <!-- 음식점 선택 (여러 음식점 운영하는 경우) -->
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

        <!-- 빠른 설정 버튼들 -->
        <div class="quick-actions" v-if="selectedStoreId">
          <h2 class="section-title">빠른 설정</h2>
          <div class="quick-buttons">
            <ion-button 
              fill="outline" 
              size="small" 
              @click="setAllSameHours"
              class="quick-button"
            >
              <ion-icon :icon="timeOutline" slot="start"></ion-icon>
              전체 동일 시간
            </ion-button>
            <ion-button 
              fill="outline" 
              size="small" 
              @click="setWeekdayHours"
              class="quick-button"
            >
              <ion-icon :icon="businessOutline" slot="start"></ion-icon>
              평일만 영업
            </ion-button>
            <ion-button 
              fill="outline" 
              size="small" 
              @click="clearAllHours"
              class="quick-button"
            >
              <ion-icon :icon="trashOutline" slot="start"></ion-icon>
              전체 초기화
            </ion-button>
          </div>
        </div>

        <!-- 요일별 운영시간 설정 -->
        <div class="form-section" v-if="selectedStoreId">
          <h2 class="section-title">요일별 운영시간</h2>
          
          <div class="day-cards">
            <div 
              v-for="(day, index) in daysOfWeek" 
              :key="day.value"
              class="day-card"
              :class="{ 'day-card-closed': !operatingHours[day.value].isOpen }"
            >
              <!-- 요일 헤더 -->
              <div class="day-header">
                <div class="day-info">
                  <h3 class="day-name">{{ day.label }}</h3>
                  <p class="day-name-en">{{ day.labelEn }}</p>
                </div>
                <ion-toggle
                  v-model="operatingHours[day.value].isOpen"
                  @ionChange="onDayToggle(day.value)"
                  class="day-toggle"
                ></ion-toggle>
              </div>

              <!-- 운영시간 설정 (영업일인 경우) -->
              <div v-if="operatingHours[day.value].isOpen" class="day-content">
                <!-- 영업시간 -->
                <div class="time-section">
                  <h4 class="time-section-title">
                    <ion-icon :icon="timeOutline"></ion-icon>
                    영업시간
                  </h4>
                  <div class="time-inputs">
                    <ion-item class="time-item">
                      <ion-datetime-button
                        :datetime="`open-time-${day.value}`"
                        class="time-button"
                      ></ion-datetime-button>
                      <ion-modal :keep-contents-mounted="true">
                        <ion-datetime
                          :id="`open-time-${day.value}`"
                          presentation="time"
                          :value="operatingHours[day.value].openTime"
                          @ionChange="updateOpenTime(day.value, $event)"
                        ></ion-datetime>
                      </ion-modal>
                      <div slot="label">오픈</div>
                    </ion-item>
                    
                    <div class="time-divider">
                      <ion-icon :icon="removeOutline"></ion-icon>
                    </div>
                    
                    <ion-item class="time-item">
                      <ion-datetime-button
                        :datetime="`close-time-${day.value}`"
                        class="time-button"
                      ></ion-datetime-button>
                      <ion-modal :keep-contents-mounted="true">
                        <ion-datetime
                          :id="`close-time-${day.value}`"
                          presentation="time"
                          :value="operatingHours[day.value].closeTime"
                          @ionChange="updateCloseTime(day.value, $event)"
                        ></ion-datetime>
                      </ion-modal>
                      <div slot="label">마감</div>
                    </ion-item>
                  </div>
                </div>

                <!-- 브레이크 타임 -->
                <div class="break-time-section">
                  <div class="break-time-header">
                    <h4 class="time-section-title">
                      <ion-icon :icon="pauseOutline"></ion-icon>
                      브레이크 타임
                    </h4>
                    <ion-toggle
                      v-model="operatingHours[day.value].isBreakTime"
                      @ionChange="onBreakTimeToggle(day.value)"
                      class="break-toggle"
                    ></ion-toggle>
                  </div>
                  
                  <div v-if="operatingHours[day.value].isBreakTime" class="time-inputs">
                    <ion-item class="time-item">
                      <ion-datetime-button
                        :datetime="`break-start-${day.value}`"
                        class="time-button"
                      ></ion-datetime-button>
                      <ion-modal :keep-contents-mounted="true">
                        <ion-datetime
                          :id="`break-start-${day.value}`"
                          presentation="time"
                          :value="operatingHours[day.value].breakStartTime"
                          @ionChange="updateBreakStartTime(day.value, $event)"
                        ></ion-datetime>
                      </ion-modal>
                      <div slot="label">시작</div>
                    </ion-item>
                    
                    <div class="time-divider">
                      <ion-icon :icon="removeOutline"></ion-icon>
                    </div>
                    
                    <ion-item class="time-item">
                      <ion-datetime-button
                        :datetime="`break-end-${day.value}`"
                        class="time-button"
                      ></ion-datetime-button>
                      <ion-modal :keep-contents-mounted="true">
                        <ion-datetime
                          :id="`break-end-${day.value}`"
                          presentation="time"
                          :value="operatingHours[day.value].breakEndTime"
                          @ionChange="updateBreakEndTime(day.value, $event)"
                        ></ion-datetime>
                      </ion-modal>
                      <div slot="label">종료</div>
                    </ion-item>
                  </div>
                </div>
              </div>

              <!-- 휴무일 메시지 -->
              <div v-else class="closed-message">
                <ion-icon :icon="moonOutline"></ion-icon>
                <span>휴무일</span>
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
            @click="saveAllOperatingHours"
            :disabled="isLoading"
          >
            <ion-spinner v-if="isLoading" name="crescent"></ion-spinner>
            <span v-else>운영시간 저장</span>
          </ion-button>
        </div>

        <!-- 빈 상태 (음식점 선택되지 않음) -->
        <div v-if="!selectedStoreId && stores.length > 0" class="empty-state">
          <ion-icon :icon="storefront" class="empty-icon"></ion-icon>
          <h3>음식점을 선택해주세요</h3>
          <p>운영시간을 설정할 음식점을 선택하세요.</p>
        </div>

        <!-- 빈 상태 (등록된 음식점 없음) -->
        <div v-if="stores.length === 0" class="empty-state">
          <ion-icon :icon="addCircleOutline" class="empty-icon"></ion-icon>
          <h3>등록된 음식점이 없습니다</h3>
          <p>먼저 음식점을 등록해주세요.</p>
          <ion-button 
            fill="outline" 
            @click="$router.push('/store/registration')"
            class="action-button"
          >
            음식점 등록하기
          </ion-button>
        </div>
      </div>
    </ion-content>

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
import { ref, reactive, computed, onMounted, watch } from 'vue'
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
  IonSelect,
  IonSelectOption,
  IonToggle,
  IonDatetime,
  IonDatetimeButton,
  IonModal,
  IonSpinner,
  IonLoading,
  IonToast,
  alertController
} from '@ionic/vue'
import {
  saveOutline,
  timeOutline,
  businessOutline,
  trashOutline,
  removeOutline,
  pauseOutline,
  moonOutline,
  storefront,
  addCircleOutline
} from 'ionicons/icons'
import axios from 'axios'

// 라우터
const router = useRouter()

// 상태 관리
const isLoading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const selectedStoreId = ref<number | null>(null)
const stores = ref<any[]>([])

// 요일 정보
const daysOfWeek = [
  { label: '월요일', labelEn: 'Monday', value: 'MONDAY' },
  { label: '화요일', labelEn: 'Tuesday', value: 'TUESDAY' },
  { label: '수요일', labelEn: 'Wednesday', value: 'WEDNESDAY' },
  { label: '목요일', labelEn: 'Thursday', value: 'THURSDAY' },
  { label: '금요일', labelEn: 'Friday', value: 'FRIDAY' },
  { label: '토요일', labelEn: 'Saturday', value: 'SATURDAY' },
  { label: '일요일', labelEn: 'Sunday', value: 'SUNDAY' }
]

// 운영시간 데이터
const operatingHours = reactive<Record<string, any>>({
  MONDAY: { isOpen: true, openTime: '09:00', closeTime: '22:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  TUESDAY: { isOpen: true, openTime: '09:00', closeTime: '22:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  WEDNESDAY: { isOpen: true, openTime: '09:00', closeTime: '22:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  THURSDAY: { isOpen: true, openTime: '09:00', closeTime: '22:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  FRIDAY: { isOpen: true, openTime: '09:00', closeTime: '22:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  SATURDAY: { isOpen: true, openTime: '10:00', closeTime: '23:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' },
  SUNDAY: { isOpen: false, openTime: '10:00', closeTime: '21:00', isBreakTime: false, breakStartTime: '15:00', breakEndTime: '16:00' }
})

// 컴포넌트 마운트 시
onMounted(async () => {
  await loadStores()
  if (stores.value.length === 1) {
    selectedStoreId.value = stores.value[0].id
    await loadOperatingHours()
  }
})

// 음식점 변경 감지
watch(selectedStoreId, async (newStoreId) => {
  if (newStoreId) {
    await loadOperatingHours()
  }
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

const loadOperatingHours = async () => {
  if (!selectedStoreId.value) return
  
  try {
    const response = await axios.get(`/api/v1/stores/${selectedStoreId.value}/operating-hours`)
    
    // 기존 운영시간 데이터가 있으면 적용
    if (response.data && response.data.length > 0) {
      response.data.forEach((hour: any) => {
        if (operatingHours[hour.dayOfWeek]) {
          operatingHours[hour.dayOfWeek] = {
            id: hour.id,
            isOpen: hour.isOpen,
            openTime: hour.openTime || '09:00',
            closeTime: hour.closeTime || '22:00',
            isBreakTime: hour.isBreakTime || false,
            breakStartTime: hour.breakStartTime || '15:00',
            breakEndTime: hour.breakEndTime || '16:00'
          }
        }
      })
    }
  } catch (error) {
    console.error('운영시간 로드 실패:', error)
    showToastMessage('운영시간 정보를 불러오는데 실패했습니다.')
  }
}

const onStoreChange = () => {
  // 음식점이 변경되면 운영시간을 다시 로드
  loadOperatingHours()
}

const onDayToggle = (dayValue: string) => {
  // 영업일 토글
  console.log(`${dayValue} 영업 상태 변경:`, operatingHours[dayValue].isOpen)
}

const onBreakTimeToggle = (dayValue: string) => {
  // 브레이크 타임 토글
  console.log(`${dayValue} 브레이크 타임 변경:`, operatingHours[dayValue].isBreakTime)
}

const updateOpenTime = (dayValue: string, event: any) => {
  operatingHours[dayValue].openTime = event.detail.value.split('T')[1].substring(0, 5)
}

const updateCloseTime = (dayValue: string, event: any) => {
  operatingHours[dayValue].closeTime = event.detail.value.split('T')[1].substring(0, 5)
}

const updateBreakStartTime = (dayValue: string, event: any) => {
  operatingHours[dayValue].breakStartTime = event.detail.value.split('T')[1].substring(0, 5)
}

const updateBreakEndTime = (dayValue: string, event: any) => {
  operatingHours[dayValue].breakEndTime = event.detail.value.split('T')[1].substring(0, 5)
}

// 빠른 설정 메소드들
const setAllSameHours = async () => {
  const alert = await alertController.create({
    header: '전체 동일 시간 설정',
    inputs: [
      {
        name: 'openTime',
        type: 'time',
        placeholder: '오픈 시간',
        value: '09:00'
      },
      {
        name: 'closeTime',
        type: 'time',
        placeholder: '마감 시간',
        value: '22:00'
      }
    ],
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '적용',
        handler: (data) => {
          daysOfWeek.forEach(day => {
            operatingHours[day.value].isOpen = true
            operatingHours[day.value].openTime = data.openTime
            operatingHours[day.value].closeTime = data.closeTime
          })
          showToastMessage('전체 동일 시간이 적용되었습니다.')
        }
      }
    ]
  })
  await alert.present()
}

const setWeekdayHours = () => {
  // 월~금요일만 영업으로 설정
  daysOfWeek.forEach(day => {
    if (['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'].includes(day.value)) {
      operatingHours[day.value].isOpen = true
      operatingHours[day.value].openTime = '09:00'
      operatingHours[day.value].closeTime = '22:00'
    } else {
      operatingHours[day.value].isOpen = false
    }
  })
  showToastMessage('평일 영업 시간이 설정되었습니다.')
}

const clearAllHours = async () => {
  const alert = await alertController.create({
    header: '전체 초기화',
    message: '모든 운영시간을 초기화하시겠습니까?',
    buttons: [
      {
        text: '취소',
        role: 'cancel'
      },
      {
        text: '초기화',
        handler: () => {
          daysOfWeek.forEach(day => {
            operatingHours[day.value].isOpen = false
          })
          showToastMessage('운영시간이 초기화되었습니다.')
        }
      }
    ]
  })
  await alert.present()
}

const saveAllOperatingHours = async () => {
  if (!selectedStoreId.value) {
    showToastMessage('음식점을 선택해주세요.')
    return
  }

  isLoading.value = true
  
  try {
    // 운영시간 데이터를 API 형식으로 변환
    const operatingHourData = daysOfWeek.map(day => ({
      dayOfWeek: day.value,
      isOpen: operatingHours[day.value].isOpen,
      openTime: operatingHours[day.value].isOpen ? operatingHours[day.value].openTime : null,
      closeTime: operatingHours[day.value].isOpen ? operatingHours[day.value].closeTime : null,
      isBreakTime: operatingHours[day.value].isOpen ? operatingHours[day.value].isBreakTime : false,
      breakStartTime: operatingHours[day.value].isBreakTime ? operatingHours[day.value].breakStartTime : null,
      breakEndTime: operatingHours[day.value].isBreakTime ? operatingHours[day.value].breakEndTime : null
    }))

    // 일괄 저장 API 호출
    await axios.post(`/api/v1/stores/${selectedStoreId.value}/operating-hours/batch`, operatingHourData)
    
    showToastMessage('운영시간이 성공적으로 저장되었습니다!')
    
    // 저장 후 데이터 다시 로드
    await loadOperatingHours()
  } catch (error) {
    console.error('운영시간 저장 실패:', error)
    showToastMessage('운영시간 저장에 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

const showToastMessage = (message: string) => {
  toastMessage.value = message
  showToast.value = true
}
</script>

<style scoped>
.operating-hour-content {
  --background: #f8f9fa;
}

.responsive-container {
  max-width: 800px;
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

.quick-actions {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quick-button {
  flex: 1;
  min-width: 140px;
}

.day-cards {
  display: grid;
  gap: 20px;
}

.day-card {
  background: white;
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s ease;
}

.day-card-closed {
  background: #f8f9fa;
  border-color: #dee2e6;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.day-info h3 {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.day-info p {
  font-size: 0.9rem;
  color: #666;
  margin: 2px 0 0 0;
}

.day-content {
  padding: 20px;
}

.time-section {
  margin-bottom: 20px;
}

.time-section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #495057;
  margin: 0 0 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-inputs {
  display: flex;
  align-items: center;
  gap: 15px;
}

.time-item {
  flex: 1;
  --background: #f8f9fa;
  --border-radius: 8px;
}

.time-button {
  width: 100%;
  text-align: center;
}

.time-divider {
  display: flex;
  align-items: center;
  color: #adb5bd;
  font-size: 1.2rem;
}

.break-time-section {
  border-top: 1px solid #e9ecef;
  padding-top: 20px;
}

.break-time-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.closed-message {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 30px;
  color: #6c757d;
  font-size: 1rem;
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

.action-button {
  margin-top: 15px;
}

.form-item {
  --background: #f8f9fa;
  --border-radius: 8px;
  margin-bottom: 15px;
}

.required {
  color: #dc3545;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .responsive-container {
    padding: 15px;
  }
  
  .time-inputs {
    flex-direction: column;
    gap: 10px;
  }
  
  .time-divider {
    transform: rotate(90deg);
  }
  
  .quick-buttons {
    flex-direction: column;
  }
  
  .quick-button {
    min-width: auto;
  }
}
</style> 