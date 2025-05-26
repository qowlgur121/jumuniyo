<template>
  <ion-page class="menu-management-page">
    <ion-header>
      <ion-toolbar>
        <ion-buttons slot="start">
          <ion-back-button default-href="/owner/dashboard"></ion-back-button>
        </ion-buttons>
        <ion-title style="text-align: center;">메뉴 관리</ion-title>
        <ion-buttons slot="end">
          <ion-button @click="openAddCategoryModal">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-button>
        </ion-buttons>
      </ion-toolbar>
    </ion-header>

    <ion-content>
      <!-- 카테고리 탭 -->
      <ion-segment v-model="selectedCategoryId" @ionChange="onCategoryChange" v-if="categories.length > 0">
        <ion-segment-button 
          v-for="category in categories" 
          :key="category.id" 
          :value="category.id.toString()"
        >
          <ion-label>{{ category.name }}</ion-label>
        </ion-segment-button>
      </ion-segment>

      <!-- 메뉴 목록 -->
      <div class="menu-list" v-if="selectedCategoryId && currentMenus.length > 0">
        <ion-card v-for="menu in currentMenus" :key="menu.id" class="menu-card">
          <ion-card-content>
            <div class="menu-header">
              <div class="menu-info">
                <img v-if="menu.imageUrl" :src="menu.imageUrl" class="menu-image" />
                <div class="menu-placeholder" v-else>
                  <ion-icon :icon="imageOutline"></ion-icon>
                </div>
                <div class="menu-details">
                  <h3>{{ menu.name }}</h3>
                  <p class="menu-description">{{ menu.description }}</p>
                  <p class="menu-price">{{ menu.formattedPrice }}</p>
                </div>
              </div>
              <div class="menu-actions">
                <ion-toggle 
                  :checked="menu.isAvailable" 
                  @ionChange="toggleMenuAvailability(menu)"
                ></ion-toggle>
              </div>
            </div>
            
            <div class="menu-footer">
              <ion-chip :color="menu.isRecommended ? 'primary' : 'medium'">
                <ion-icon :icon="starOutline"></ion-icon>
                <ion-label>{{ menu.isRecommended ? '추천' : '일반' }}</ion-label>
              </ion-chip>
              <ion-chip color="medium">
                <ion-label>판매: {{ menu.soldCount }}개</ion-label>
              </ion-chip>
              <div class="menu-buttons">
                <ion-button fill="clear" size="small" @click="editMenu(menu)">
                  <ion-icon :icon="createOutline"></ion-icon>
                </ion-button>
                <ion-button fill="clear" size="small" color="danger" @click="deleteMenu(menu)">
                  <ion-icon :icon="trashOutline"></ion-icon>
                </ion-button>
              </div>
            </div>
          </ion-card-content>
        </ion-card>

        <!-- 메뉴 추가 버튼 -->
        <ion-fab vertical="bottom" horizontal="end" slot="fixed">
          <ion-fab-button @click="openAddMenuModal">
            <ion-icon :icon="addOutline"></ion-icon>
          </ion-fab-button>
        </ion-fab>
      </div>

      <!-- 메뉴가 없을 때 -->
      <div v-else-if="selectedCategoryId && currentMenus.length === 0" class="empty-state">
        <ion-icon :icon="restaurantOutline" class="empty-icon"></ion-icon>
        <h2>메뉴를 추가해주세요</h2>
        <p>이 카테고리에 등록된 메뉴가 없습니다.</p>
        <ion-button @click="openAddMenuModal">
          메뉴 추가
        </ion-button>
      </div>

      <!-- 카테고리가 없을 때 -->
      <div v-else class="empty-state">
        <ion-icon :icon="restaurantOutline" class="empty-icon"></ion-icon>
        <h2>카테고리를 먼저 추가해주세요</h2>
        <p>메뉴를 등록하기 전에 카테고리를 생성해야 합니다.</p>
        <ion-button @click="openAddCategoryModal">
          카테고리 추가
        </ion-button>
      </div>
    </ion-content>

    <!-- 카테고리 추가/수정 모달 -->
    <ion-modal :is-open="isCategoryModalOpen" @didDismiss="closeCategoryModal">
      <ion-header>
        <ion-toolbar>
          <ion-title style="text-align: center;">{{ editingCategory ? '카테고리 수정' : '카테고리 추가' }}</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeCategoryModal">닫기</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <ion-item>
          <ion-input 
            v-model="categoryForm.name" 
            label="카테고리 이름" 
            label-placement="stacked"
            placeholder="예: 메인메뉴, 사이드메뉴"
          ></ion-input>
        </ion-item>
        <ion-item>
          <ion-textarea 
            v-model="categoryForm.description" 
            label="설명 (선택사항)" 
            label-placement="stacked"
            placeholder="카테고리에 대한 설명을 입력하세요"
          ></ion-textarea>
        </ion-item>
        <div class="modal-buttons">
          <ion-button expand="block" @click="saveCategory">
            {{ editingCategory ? '수정' : '추가' }}
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>

    <!-- 메뉴 추가/수정 모달 -->
    <ion-modal :is-open="isMenuModalOpen" @didDismiss="closeMenuModal">
      <ion-header>
        <ion-toolbar>
          <ion-title style="text-align: center;">{{ editingMenu ? '메뉴 수정' : '메뉴 추가' }}</ion-title>
          <ion-buttons slot="end">
            <ion-button @click="closeMenuModal">닫기</ion-button>
          </ion-buttons>
        </ion-toolbar>
      </ion-header>
      <ion-content>
        <ion-item>
          <ion-input 
            v-model="menuForm.name" 
            label="메뉴 이름" 
            label-placement="stacked"
            placeholder="메뉴 이름을 입력하세요"
          ></ion-input>
        </ion-item>
        <ion-item>
          <ion-textarea 
            v-model="menuForm.description" 
            label="메뉴 설명" 
            label-placement="stacked"
            placeholder="메뉴에 대한 설명을 입력하세요"
          ></ion-textarea>
        </ion-item>

        <!-- 메뉴 이미지 업로드 -->
        <ion-item>
          <div class="image-upload-section">
            <ion-label>메뉴 이미지</ion-label>
            <ImageUploader
              image-type="menu"
              :store-id="storeId"
              :menu-id="editingMenu?.id"
              :existing-image-url="editingMenu?.imageUrl"
              @image-selected="onMenuImageSelected"
              @upload-success="onMenuImageUploadSuccess"
              @upload-error="onMenuImageUploadError"
              @image-removed="onMenuImageRemoved"
            />
          </div>
        </ion-item>
        <ion-item>
          <ion-input 
            v-model="menuForm.price" 
            type="number" 
            label="가격" 
            label-placement="stacked"
            placeholder="0"
          ></ion-input>
        </ion-item>
        <ion-item>
          <ion-checkbox v-model="menuForm.isRecommended"></ion-checkbox>
          <ion-label class="ion-margin-start">추천 메뉴로 설정</ion-label>
        </ion-item>
        <div class="modal-buttons">
          <ion-button expand="block" @click="saveMenu">
            {{ editingMenu ? '수정' : '추가' }}
          </ion-button>
        </div>
      </ion-content>
    </ion-modal>
  </ion-page>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import {
  IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton,
  IonButton, IonIcon, IonSegment, IonSegmentButton, IonLabel, IonCard, IonCardContent,
  IonToggle, IonChip, IonFab, IonFabButton, IonModal, IonItem, IonInput, IonTextarea,
  IonCheckbox, alertController, toastController
} from '@ionic/vue'
import {
  addOutline, imageOutline, starOutline, createOutline, trashOutline,
  restaurantOutline
} from 'ionicons/icons'
import { useRouter, useRoute } from 'vue-router'
import { menuApi } from '@/services/api'
import ImageUploader from '@/components/ImageUploader.vue'

export default {
  name: 'MenuManagementPage',
  components: {
    IonPage, IonHeader, IonToolbar, IonTitle, IonContent, IonButtons, IonBackButton,
    IonButton, IonIcon, IonSegment, IonSegmentButton, IonLabel, IonCard, IonCardContent,
    IonToggle, IonChip, IonFab, IonFabButton, IonModal, IonItem, IonInput, IonTextarea,
    IonCheckbox, ImageUploader
  },
  setup() {
    // 반응형 데이터
    const router = useRouter()
    const route = useRoute()
    const storeId = computed(() => parseInt(route.params.storeId))
    const categories = ref([])
    const menus = ref([])
    const selectedCategoryId = ref(null)

    // 모달 상태
    const isCategoryModalOpen = ref(false)
    const isMenuModalOpen = ref(false)
    const editingCategory = ref(null)
    const editingMenu = ref(null)

    // 폼 데이터
    const categoryForm = ref({
      name: '',
      description: ''
    })

    const menuForm = ref({
      name: '',
      description: '',
      price: 0,
      isRecommended: false
    })

    // 계산된 속성
    const currentMenus = computed(() => {
      if (!selectedCategoryId.value) return []
      return menus.value.filter(menu => menu.categoryId === parseInt(selectedCategoryId.value))
    })

    // 메서드
    const loadCategories = async () => {
      try {
        const response = await menuApi.getMenuCategories(storeId.value)
        categories.value = response.data
        
        if (categories.value.length > 0 && !selectedCategoryId.value) {
          selectedCategoryId.value = categories.value[0].id.toString()
        }
      } catch (error) {
        console.error('카테고리 로딩 실패:', error)
        const toast = await toastController.create({
          message: '카테고리를 불러오는데 실패했습니다.',
          duration: 2000,
          color: 'danger'
        })
        toast.present()
      }
    }

    const loadMenus = async () => {
      try {
        const response = await menuApi.getMenus(storeId.value)
        menus.value = response.data
      } catch (error) {
        console.error('메뉴 로딩 실패:', error)
        const toast = await toastController.create({
          message: '메뉴를 불러오는데 실패했습니다.',
          duration: 2000,
          color: 'danger'
        })
        toast.present()
      }
    }

    const onCategoryChange = (event) => {
      selectedCategoryId.value = event.detail.value
    }

    const openAddCategoryModal = () => {
      editingCategory.value = null
      categoryForm.value = { name: '', description: '' }
      isCategoryModalOpen.value = true
    }

    const closeCategoryModal = () => {
      isCategoryModalOpen.value = false
      editingCategory.value = null
    }

    const saveCategory = async () => {
      try {
        if (editingCategory.value) {
          await menuApi.updateMenuCategory(storeId.value, editingCategory.value.id, categoryForm.value)
        } else {
          await menuApi.createMenuCategory(storeId.value, categoryForm.value)
        }
        
        await loadCategories()
        closeCategoryModal()
        
        const toast = await toastController.create({
          message: `카테고리가 ${editingCategory.value ? '수정' : '추가'}되었습니다.`,
          duration: 2000,
          color: 'success'
        })
        toast.present()
      } catch (error) {
        console.error('카테고리 저장 실패:', error)
        const toast = await toastController.create({
          message: '카테고리 저장에 실패했습니다.',
          duration: 2000,
          color: 'danger'
        })
        toast.present()
      }
    }

    const openAddMenuModal = () => {
      if (!selectedCategoryId.value) {
        const toast = toastController.create({
          message: '카테고리를 먼저 선택해주세요.',
          duration: 2000,
          color: 'warning'
        })
        toast.then(t => t.present())
        return
      }
      
      editingMenu.value = null
      menuForm.value = {
        name: '',
        description: '',
        price: 0,
        isRecommended: false
      }
      isMenuModalOpen.value = true
    }

    const closeMenuModal = () => {
      isMenuModalOpen.value = false
      editingMenu.value = null
    }

    const editMenu = (menu) => {
      editingMenu.value = menu
      menuForm.value = {
        name: menu.name,
        description: menu.description || '',
        price: menu.price,
        isRecommended: menu.isRecommended
      }
      isMenuModalOpen.value = true
    }

    const saveMenu = async () => {
      try {
        const menuData = {
          ...menuForm.value,
          categoryId: parseInt(selectedCategoryId.value)
        }
        
        if (editingMenu.value) {
          await menuApi.updateMenu(storeId.value, editingMenu.value.id, menuData)
        } else {
          await menuApi.createMenu(storeId.value, menuData)
        }
        
        await loadMenus()
        closeMenuModal()
        
        const toast = await toastController.create({
          message: `메뉴가 ${editingMenu.value ? '수정' : '추가'}되었습니다.`,
          duration: 2000,
          color: 'success'
        })
        toast.present()
      } catch (error) {
        console.error('메뉴 저장 실패:', error)
        const toast = await toastController.create({
          message: '메뉴 저장에 실패했습니다.',
          duration: 2000,
          color: 'danger'
        })
        toast.present()
      }
    }

    const toggleMenuAvailability = async (menu) => {
      try {
        await menuApi.toggleMenuAvailability(storeId.value, menu.id)
        
        // 로컬 상태 업데이트
        menu.isAvailable = !menu.isAvailable
        
        const toast = await toastController.create({
          message: `${menu.name}이(가) ${menu.isAvailable ? '판매 시작' : '판매 중단'}되었습니다.`,
          duration: 2000,
          color: 'success'
        })
        toast.present()
      } catch (error) {
        console.error('메뉴 상태 변경 실패:', error)
        const toast = await toastController.create({
          message: '메뉴 상태 변경에 실패했습니다.',
          duration: 2000,
          color: 'danger'
        })
        toast.present()
      }
    }

    const deleteMenu = async (menu) => {
      const alert = await alertController.create({
        header: '메뉴 삭제',
        message: `'${menu.name}' 메뉴를 삭제하시겠습니까?`,
        buttons: [
          {
            text: '취소',
            role: 'cancel'
          },
          {
            text: '삭제',
            role: 'destructive',
            handler: async () => {
              try {
                await menuApi.deleteMenu(storeId.value, menu.id)
                await loadMenus()
                
                const toast = await toastController.create({
                  message: '메뉴가 삭제되었습니다.',
                  duration: 2000,
                  color: 'success'
                })
                toast.present()
              } catch (error) {
                console.error('메뉴 삭제 실패:', error)
                const toast = await toastController.create({
                  message: '메뉴 삭제에 실패했습니다.',
                  duration: 2000,
                  color: 'danger'
                })
                toast.present()
              }
            }
          }
        ]
      })
      
      await alert.present()
    }

    // 이미지 업로드 이벤트 핸들러
    const onMenuImageSelected = (file) => {
      console.log('메뉴 이미지 선택됨:', file)
    }

    const onMenuImageUploadSuccess = (response) => {
      console.log('메뉴 이미지 업로드 성공:', response)
      // 메뉴 목록 새로고침
      loadMenus()
    }

    const onMenuImageUploadError = (error) => {
      console.error('메뉴 이미지 업로드 실패:', error)
    }

    const onMenuImageRemoved = () => {
      console.log('메뉴 이미지 제거됨')
    }

    // 라이프사이클
    onMounted(() => {
      loadCategories()
      loadMenus()
    })

    return {
      // 아이콘
      addOutline, imageOutline, starOutline, createOutline, trashOutline, restaurantOutline,
      // 데이터
      categories, menus, selectedCategoryId, currentMenus,
      // 모달 상태
      isCategoryModalOpen, isMenuModalOpen, editingCategory, editingMenu,
      // 폼 데이터
      categoryForm, menuForm,
      // 메서드
      onCategoryChange, openAddCategoryModal, closeCategoryModal, saveCategory,
      openAddMenuModal, closeMenuModal, editMenu, saveMenu,
      toggleMenuAvailability, deleteMenu,
      // 이미지 업로드 이벤트 핸들러
      onMenuImageSelected, onMenuImageUploadSuccess, onMenuImageUploadError, onMenuImageRemoved,
      // 계산된 속성
      storeId
    }
  }
}
</script>

<style scoped>
/* 요기요 스타일 색상 변수 */
.menu-management-page {
  --yogiyo-primary: #ff1744;
  --yogiyo-secondary: #e50032;
  --yogiyo-background: #ffffff;
  --yogiyo-light-gray: #f8f9fa;
  --yogiyo-border-gray: #e9ecef;
  --yogiyo-text-primary: #333333;
  --yogiyo-text-secondary: #666666;
  --yogiyo-text-placeholder: #999999;
}

/* 헤더 스타일 개선 */
.menu-management-page ion-header ion-toolbar {
  --background: linear-gradient(135deg, #ff1744 0%, #e50032 100%);
  --color: white;
}

.menu-management-page ion-header ion-title {
  font-weight: 600;
  font-size: 18px;
  color: white;
}

.menu-management-page ion-header ion-button {
  --color: white;
}

/* 카테고리 세그먼트 스타일 */
.menu-management-page ion-segment {
  margin: 16px;
  --background: #f8f9fa;
  border-radius: 12px;
  padding: 4px;
}

.menu-management-page ion-segment-button {
  --color: #666666;
  --color-checked: white;
  --background-checked: #ff1744;
  --indicator-color: transparent;
  border-radius: 8px;
  margin: 2px;
  font-weight: 500;
}

.menu-management-page .menu-list {
  padding: 16px;
  background-color: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.menu-management-page .menu-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  --background: white;
  border: none;
  background: white;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.menu-info {
  display: flex;
  flex: 1;
}

.menu-management-page .menu-image {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  object-fit: cover;
  margin-right: 16px;
  border: 2px solid #e9ecef;
}

.menu-management-page .menu-placeholder {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  border: 2px solid #e9ecef;
}

.menu-management-page .menu-placeholder ion-icon {
  font-size: 28px;
  color: #999999;
}

.menu-details {
  flex: 1;
}

.menu-management-page .menu-details h3 {
  margin: 0 0 6px 0;
  font-size: 17px;
  font-weight: 600;
  color: #333333;
  line-height: 1.3;
}

.menu-management-page .menu-description {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #666666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.menu-management-page .menu-price {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #ff1744;
}

.menu-actions {
  display: flex;
  align-items: center;
}

.menu-management-page .menu-actions ion-toggle {
  --background: #e9ecef;
  --background-checked: #ff1744;
  --handle-background: white;
  --handle-background-checked: white;
  transform: scale(0.9);
}

.menu-management-page .menu-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.menu-footer ion-chip {
  font-size: 12px;
  height: 28px;
  border-radius: 14px;
}

.menu-management-page .menu-footer ion-chip[color="primary"] {
  --background: #ff1744;
  --color: white;
}

.menu-management-page .menu-footer ion-chip[color="medium"] {
  --background: #f8f9fa;
  --color: #666666;
}

.menu-buttons {
  display: flex;
  gap: 4px;
}

.menu-buttons ion-button {
  --border-radius: 8px;
  height: 32px;
  width: 32px;
}

/* FAB 버튼 스타일 */
.menu-management-page ion-fab-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --color: white;
  --box-shadow: 0 4px 16px rgba(255, 23, 68, 0.3);
}

/* 빈 상태 스타일 */
.menu-management-page .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  text-align: center;
  background-color: #f8f9fa;
  min-height: calc(100vh - 200px);
}

.menu-management-page .empty-icon {
  font-size: 80px;
  color: #999999;
  margin-bottom: 24px;
  opacity: 0.7;
}

.menu-management-page .empty-state h2 {
  margin: 0 0 12px 0;
  color: #333333;
  font-size: 20px;
  font-weight: 600;
}

.menu-management-page .empty-state p {
  margin: 0 0 32px 0;
  color: #666666;
  font-size: 16px;
  line-height: 1.5;
}

.menu-management-page .empty-state ion-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --color: white;
  --border-radius: 12px;
  height: 48px;
  font-weight: 600;
  font-size: 16px;
  margin-top: 16px;
}

/* 모달 스타일 */
.modal-buttons {
  padding: 16px;
}

.menu-management-page .modal-buttons ion-button {
  --background: #ff1744;
  --background-activated: #e50032;
  --color: white;
  --border-radius: 12px;
  height: 48px;
  font-weight: 600;
  font-size: 16px;
}

/* 입력 필드 스타일 */
.menu-management-page ion-item {
  --background: white;
  --border-color: #e9ecef;
  --border-radius: 8px;
  margin-bottom: 12px;
  border-radius: 8px;
}

.menu-management-page ion-input, 
.menu-management-page ion-textarea {
  --color: #333333;
  --placeholder-color: #999999;
}

/* 체크박스 스타일 */
.menu-management-page ion-checkbox {
  --background: white;
  --background-checked: #ff1744;
  --border-color: #e9ecef;
  --border-color-checked: #ff1744;
  --checkmark-color: white;
}

/* 이미지 업로드 섹션 스타일 */
.image-upload-section {
  width: 100%;
  padding: 16px 0;
}

.image-upload-section ion-label {
  display: block;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333333;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .menu-list {
    padding: 12px;
  }
  
  .menu-card {
    margin-bottom: 12px;
  }
  
  .menu-image, .menu-placeholder {
    width: 60px;
    height: 60px;
    margin-right: 12px;
  }
  
  .menu-details h3 {
    font-size: 16px;
  }
  
  .menu-price {
    font-size: 16px;
  }
  
  .empty-state {
    padding: 48px 24px;
  }
  
  .empty-icon {
    font-size: 64px;
  }
}

/* 애니메이션 효과 */
.menu-card {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

ion-button {
  transition: transform 0.1s ease;
}

ion-button:active {
  transform: scale(0.95);
}

/* 로딩 상태 스타일 */
.loading-skeleton {
  background: linear-gradient(90deg, var(--yogiyo-light-gray) 25%, var(--yogiyo-border-gray) 50%, var(--yogiyo-light-gray) 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style> 