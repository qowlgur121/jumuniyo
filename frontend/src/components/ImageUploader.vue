<template>
  <div class="image-uploader">
    <!-- 이미지 미리보기 영역 -->
    <div class="preview-container" :class="{ 'has-image': previewUrl || existingImageUrl }">
      <div v-if="previewUrl || existingImageUrl" class="image-preview">
        <ion-img 
          :src="previewUrl || existingImageUrl" 
          :alt="imageType === 'logo' ? '가게 로고' : '메뉴 이미지'"
          class="preview-image"
        />
        <ion-button 
          fill="clear" 
          size="small" 
          class="remove-button"
          @click="removeImage"
          :disabled="isUploading"
        >
          <ion-icon :icon="closeOutline" />
        </ion-button>
      </div>
      
      <!-- 이미지 선택 영역 -->
      <div v-else class="upload-placeholder" @click="triggerFileInput">
        <ion-icon :icon="cameraOutline" size="large" />
        <p>{{ placeholderText }}</p>
        <ion-button fill="outline" size="small">
          이미지 선택
        </ion-button>
      </div>
    </div>

    <!-- 숨겨진 파일 입력 -->
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      @change="onFileSelected"
      style="display: none"
    />

    <!-- 업로드 진행 상태 -->
    <div v-if="isUploading" class="upload-progress">
      <ion-progress-bar :value="uploadProgress / 100" />
      <p class="progress-text">업로드 중... {{ uploadProgress }}%</p>
    </div>

    <!-- 이미지 변경 버튼 (이미지가 있을 때) -->
    <div v-if="previewUrl || existingImageUrl" class="action-buttons">
      <ion-button 
        fill="outline" 
        size="small" 
        @click="triggerFileInput"
        :disabled="isUploading"
      >
        <ion-icon :icon="imageOutline" slot="start" />
        이미지 변경
      </ion-button>
      
      <ion-button 
        v-if="previewUrl && !isUploading" 
        color="primary" 
        size="small"
        @click="uploadImage"
      >
        <ion-icon :icon="cloudUploadOutline" slot="start" />
        업로드
      </ion-button>
    </div>

    <!-- 에러 메시지 -->
    <ion-text v-if="errorMessage" color="danger" class="error-message">
      <p>{{ errorMessage }}</p>
    </ion-text>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { 
  IonButton, 
  IonIcon, 
  IonImg, 
  IonProgressBar, 
  IonText,
  toastController 
} from '@ionic/vue'
import { 
  cameraOutline, 
  closeOutline, 
  imageOutline, 
  cloudUploadOutline 
} from 'ionicons/icons'
import { fileUploadApi } from '@/services/api'

// Props 정의
const props = defineProps({
  imageType: {
    type: String,
    required: true,
    validator: (value) => ['logo', 'menu'].includes(value)
  },
  existingImageUrl: {
    type: String,
    default: ''
  },
  maxFileSize: {
    type: Number,
    default: 5 * 1024 * 1024 // 5MB
  },
  allowedTypes: {
    type: Array,
    default: () => ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  },
  storeId: {
    type: Number,
    default: null
  },
  menuId: {
    type: Number,
    default: null
  }
})

// Emits 정의
const emit = defineEmits([
  'image-selected',
  'upload-start',
  'upload-success',
  'upload-error',
  'image-removed'
])

// 반응형 상태
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const isUploading = ref(false)
const uploadProgress = ref(0)
const errorMessage = ref('')

// 계산된 속성
const placeholderText = computed(() => {
  return props.imageType === 'logo' 
    ? '가게 로고를 선택해주세요' 
    : '메뉴 이미지를 선택해주세요'
})

// 파일 입력 트리거
const triggerFileInput = () => {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

// 파일 선택 처리
const onFileSelected = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 파일 유효성 검사
  if (!validateFile(file)) {
    return
  }

  selectedFile.value = file
  createPreview(file)
  emit('image-selected', file)
}

// 파일 유효성 검사
const validateFile = (file) => {
  errorMessage.value = ''

  // 파일 타입 검사
  if (!props.allowedTypes.includes(file.type)) {
    errorMessage.value = '지원하지 않는 파일 형식입니다. JPG, PNG, GIF, WebP 파일만 업로드 가능합니다.'
    return false
  }

  // 파일 크기 검사
  if (file.size > props.maxFileSize) {
    const maxSizeMB = (props.maxFileSize / (1024 * 1024)).toFixed(1)
    errorMessage.value = `파일 크기가 너무 큽니다. 최대 ${maxSizeMB}MB까지 업로드 가능합니다.`
    return false
  }

  return true
}

// 미리보기 생성
const createPreview = (file) => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  previewUrl.value = URL.createObjectURL(file)
}

// 이미지 제거
const removeImage = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
  selectedFile.value = null
  errorMessage.value = ''
  
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  
  emit('image-removed')
}

// 이미지 업로드
const uploadImage = async () => {
  if (!selectedFile.value) return

  isUploading.value = true
  uploadProgress.value = 0
  errorMessage.value = ''
  emit('upload-start')

  try {
    const formData = new FormData()
    formData.append('image', selectedFile.value)

    let response
    if (props.imageType === 'logo' && props.storeId) {
      // 가게 로고 업로드
      response = await fileUploadApi.uploadStoreLogo(props.storeId, formData, {
        onUploadProgress: (progressEvent) => {
          uploadProgress.value = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
        }
      })
    } else if (props.imageType === 'menu' && props.storeId) {
      // 메뉴 이미지 업로드
      if (props.menuId) {
        // 기존 메뉴 이미지 업데이트
        response = await fileUploadApi.updateMenuImage(props.storeId, props.menuId, formData, {
          onUploadProgress: (progressEvent) => {
            uploadProgress.value = Math.round(
              (progressEvent.loaded * 100) / progressEvent.total
            )
          }
        })
      } else {
        // 새 메뉴 이미지 업로드 (메뉴 생성과 함께)
        throw new Error('메뉴 ID가 필요합니다.')
      }
    }

    // 업로드 성공
    const toast = await toastController.create({
      message: '이미지가 성공적으로 업로드되었습니다.',
      duration: 2000,
      color: 'success'
    })
    await toast.present()

    emit('upload-success', response.data)
    
    // 미리보기 URL 정리
    if (previewUrl.value) {
      URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
    }
    selectedFile.value = null

  } catch (error) {
    console.error('이미지 업로드 실패:', error)
    errorMessage.value = error.response?.data?.message || '이미지 업로드에 실패했습니다.'
    
    const toast = await toastController.create({
      message: errorMessage.value,
      duration: 3000,
      color: 'danger'
    })
    await toast.present()

    emit('upload-error', error)
  } finally {
    isUploading.value = false
    uploadProgress.value = 0
  }
}

// 컴포넌트 언마운트 시 메모리 정리
onUnmounted(() => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
})
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.preview-container {
  position: relative;
  border: 2px dashed #e9ecef;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.preview-container.has-image {
  border: 2px solid #e9ecef;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.upload-placeholder:hover {
  background-color: #f8f9fa;
}

.upload-placeholder ion-icon {
  color: #999;
  margin-bottom: 16px;
}

.upload-placeholder p {
  margin: 8px 0 16px 0;
  color: #666;
  font-size: 14px;
}

.image-preview {
  position: relative;
  width: 100%;
  height: 200px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-button {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  --color: white;
}

.upload-progress {
  margin-top: 16px;
}

.progress-text {
  text-align: center;
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  justify-content: center;
}

.error-message {
  margin-top: 12px;
  font-size: 14px;
}

.error-message p {
  margin: 0;
}
</style> 