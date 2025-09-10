import { ref, reactive, computed, onMounted, onUnmounted, readonly } from 'vue';
import { locationService } from '@/services/locationService';

/**
 * 위치 정보 관리를 위한 Composition API
 */
export function useLocation() {
  // 반응형 상태
  const currentLocation = ref(null);
  const permissionStatus = ref('unknown');
  const isLoading = ref(false);
  const error = ref(null);
  const isWatching = ref(false);
  
  // 위치 정보 상태 객체
  const locationState = reactive({
    hasLocation: computed(() => currentLocation.value !== null),
    isLocationDenied: computed(() => permissionStatus.value === 'denied'),
    isLocationGranted: computed(() => permissionStatus.value === 'granted'),
    canRequestLocation: computed(() => permissionStatus.value === 'prompt' || permissionStatus.value === 'unknown'),
    formattedAddress: computed(() => {
      if (!currentLocation.value) return '위치 정보 없음';
      return currentLocation.value.address || '현재 위치';
    }),
    accuracy: computed(() => {
      if (!currentLocation.value || !currentLocation.value.accuracy) return null;
      return Math.round(currentLocation.value.accuracy);
    })
  });

  /**
   * 현재 위치 정보 요청
   * @param {Object} options - Geolocation 옵션
   * @returns {Promise<Object|null>} 위치 정보
   */
  const requestCurrentLocation = async (options = {}) => {
    if (isLoading.value) return null;

    isLoading.value = true;
    error.value = null;

    try {
      const location = await locationService.getCurrentPosition(options);
      currentLocation.value = location;
      return location;
    } catch (err) {
      error.value = err;
      console.error('Failed to get current location:', err);
      
      // 에러 발생 시 기본 위치 사용
      const defaultLocation = locationService.getDefaultLocation();
      currentLocation.value = defaultLocation;
      return defaultLocation;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 권한 상태 확인
   */
  const checkPermission = async () => {
    try {
      const status = await locationService.checkPermissionStatus();
      permissionStatus.value = status;
      return status;
    } catch (err) {
      console.error('Failed to check location permission:', err);
      permissionStatus.value = 'unknown';
      return 'unknown';
    }
  };

  /**
   * 저장된 위치 정보 로드
   */
  const loadSavedLocation = () => {
    const saved = locationService.getSavedLocation();
    if (saved) {
      currentLocation.value = saved;
      return true;
    }
    return false;
  };

  /**
   * 주소를 좌표로 변환하여 위치 설정
   * @param {string} address - 변환할 주소
   */
  const setLocationByAddress = async (address) => {
    if (!address) return false;

    isLoading.value = true;
    error.value = null;

    try {
      const location = await locationService.geocodeAddress(address);
      currentLocation.value = location;
      locationService.saveLocationToStorage(location);
      return location;
    } catch (err) {
      error.value = err;
      console.error('Failed to geocode address:', err);
      return false;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 위치 감시 시작
   * @param {Object} options - Geolocation 옵션
   */
  const startWatching = (options = {}) => {
    if (isWatching.value) return;

    try {
      locationService.startWatchingPosition(
        (location, watchError) => {
          if (watchError) {
            error.value = watchError;
            console.error('Location watch error:', watchError);
          } else if (location) {
            currentLocation.value = location;
            error.value = null;
          }
        },
        options
      );
      isWatching.value = true;
    } catch (err) {
      error.value = err;
      console.error('Failed to start watching location:', err);
    }
  };

  /**
   * 위치 감시 중지
   */
  const stopWatching = () => {
    if (!isWatching.value) return;

    try {
      locationService.stopWatchingPosition();
      isWatching.value = false;
    } catch (err) {
      console.error('Failed to stop watching location:', err);
    }
  };

  /**
   * 두 위치 간의 거리 계산
   * @param {number} lat - 위도
   * @param {number} lon - 경도
   * @returns {number|null} 거리 (km)
   */
  const calculateDistanceFrom = (lat, lon) => {
    if (!currentLocation.value) return null;
    
    return locationService.calculateDistance(
      currentLocation.value.latitude,
      currentLocation.value.longitude,
      lat,
      lon
    );
  };

  /**
   * 거리를 포맷된 문자열로 반환
   * @param {number} distance - 거리 (km)
   * @returns {string} 포맷된 거리
   */
  const formatDistance = (distance) => {
    return locationService.formatDistance(distance);
  };

  /**
   * 위치 정보 초기화
   */
  const resetLocation = () => {
    currentLocation.value = null;
    error.value = null;
    locationService.clearSavedLocation();
    
    if (isWatching.value) {
      stopWatching();
    }
  };

  /**
   * 기본 위치로 설정
   */
  const useDefaultLocation = () => {
    const defaultLocation = locationService.getDefaultLocation();
    currentLocation.value = defaultLocation;
    locationService.saveLocationToStorage(defaultLocation);
  };

  /**
   * 위치 정보 새로고침
   * @param {boolean} forceRefresh - 강제 새로고침 여부
   */
  const refreshLocation = async (forceRefresh = false) => {
    // 캐시된 위치가 있고 강제 새로고침이 아닌 경우
    if (!forceRefresh && loadSavedLocation()) {
      return currentLocation.value;
    }

    // 권한 확인 후 위치 요청
    await checkPermission();
    
    if (locationState.isLocationGranted || locationState.canRequestLocation) {
      return await requestCurrentLocation();
    } else {
      // 권한이 거부된 경우 기본 위치 사용
      useDefaultLocation();
      return currentLocation.value;
    }
  };

  /**
   * 자동 위치 초기화
   * 저장된 위치 -> 현재 위치 -> 기본 위치 순으로 시도
   */
  const initializeLocation = async () => {
    // 1. 저장된 위치 확인
    if (loadSavedLocation()) {
      console.log('Loaded saved location');
      return currentLocation.value;
    }

    // 2. 권한 상태 확인
    await checkPermission();

    // 3. 권한에 따른 위치 설정
    if (locationState.isLocationGranted) {
      // 권한이 이미 허용된 경우 현재 위치 요청
      return await requestCurrentLocation();
    } else if (locationState.canRequestLocation) {
      // 권한 요청 가능한 경우는 사용자 액션에 의존
      // 일단 기본 위치 사용
      useDefaultLocation();
      return currentLocation.value;
    } else {
      // 권한이 거부된 경우 기본 위치 사용
      useDefaultLocation();
      return currentLocation.value;
    }
  };

  // 생명주기 훅
  onMounted(() => {
    // 저장된 권한 상태 복원
    const savedPermission = locationService.getSavedPermissionStatus();
    if (savedPermission) {
      permissionStatus.value = savedPermission;
    }

    // 자동 위치 초기화
    initializeLocation();
  });

  onUnmounted(() => {
    // 위치 감시 중지
    if (isWatching.value) {
      stopWatching();
    }
  });

  // 반환할 API
  return {
    // 상태
    currentLocation: readonly(currentLocation),
    permissionStatus: readonly(permissionStatus),
    isLoading: readonly(isLoading),
    error: readonly(error),
    isWatching: readonly(isWatching),
    locationState: readonly(locationState),

    // 메서드
    requestCurrentLocation,
    checkPermission,
    loadSavedLocation,
    setLocationByAddress,
    startWatching,
    stopWatching,
    calculateDistanceFrom,
    formatDistance,
    resetLocation,
    useDefaultLocation,
    refreshLocation,
    initializeLocation
  };
} 