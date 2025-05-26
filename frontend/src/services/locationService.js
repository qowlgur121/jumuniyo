/**
 * 위치 서비스 - Geolocation API 및 Geocoding 기능 제공
 */

// 위치 정보 저장 키
const LOCATION_STORAGE_KEY = 'userLocation';
const LOCATION_PERMISSION_KEY = 'locationPermission';

// 위치 정보 만료 시간 (1시간)
const LOCATION_EXPIRY_TIME = 60 * 60 * 1000;

// 기본 위치 설정 (서울 시청)
const DEFAULT_LOCATION = {
  latitude: 37.5666805,
  longitude: 126.9784147,
  address: '서울특별시 중구'
};

/**
 * 위치 서비스 클래스
 */
export class LocationService {
  constructor() {
    this.currentLocation = null;
    this.permissionStatus = null;
    this.watchId = null;
  }

  /**
   * 현재 위치 정보 조회 (브라우저 Geolocation API 사용)
   * @param {Object} options - Geolocation 옵션
   * @returns {Promise<Object>} 위치 정보 객체
   */
  async getCurrentPosition(options = {}) {
    const defaultOptions = {
      enableHighAccuracy: true,
      timeout: 10000, // 10초
      maximumAge: 300000 // 5분
    };

    const geoOptions = { ...defaultOptions, ...options };

    return new Promise((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by this browser'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const location = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            timestamp: position.timestamp
          };
          
          this.currentLocation = location;
          this.saveLocationToStorage(location);
          resolve(location);
        },
        (error) => {
          console.error('Geolocation error:', error);
          reject(this.handleGeolocationError(error));
        },
        geoOptions
      );
    });
  }

  /**
   * 위치 변화 감시 시작
   * @param {Function} callback - 위치 변화 시 실행할 콜백 함수
   * @param {Object} options - Geolocation 옵션
   */
  startWatchingPosition(callback, options = {}) {
    if (!navigator.geolocation) {
      throw new Error('Geolocation is not supported');
    }

    const defaultOptions = {
      enableHighAccuracy: true,
      timeout: 15000,
      maximumAge: 60000 // 1분
    };

    const geoOptions = { ...defaultOptions, ...options };

    this.watchId = navigator.geolocation.watchPosition(
      (position) => {
        const location = {
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          accuracy: position.coords.accuracy,
          timestamp: position.timestamp
        };
        
        this.currentLocation = location;
        this.saveLocationToStorage(location);
        callback(location);
      },
      (error) => {
        console.error('Geolocation watch error:', error);
        callback(null, this.handleGeolocationError(error));
      },
      geoOptions
    );
  }

  /**
   * 위치 감시 중지
   */
  stopWatchingPosition() {
    if (this.watchId !== null) {
      navigator.geolocation.clearWatch(this.watchId);
      this.watchId = null;
    }
  }

  /**
   * 위치 권한 상태 확인
   * @returns {Promise<string>} 권한 상태 ('granted', 'denied', 'prompt')
   */
  async checkPermissionStatus() {
    if (!navigator.permissions) {
      return 'unknown';
    }

    try {
      const permission = await navigator.permissions.query({ name: 'geolocation' });
      this.permissionStatus = permission.state;
      this.savePermissionToStorage(permission.state);
      
      // 권한 상태 변화 감시
      permission.onchange = () => {
        this.permissionStatus = permission.state;
        this.savePermissionToStorage(permission.state);
      };

      return permission.state;
    } catch (error) {
      console.error('Permission check error:', error);
      return 'unknown';
    }
  }

  /**
   * 주소를 좌표로 변환 (Geocoding)
   * 현재는 간단한 구현으로, 실제 서비스에서는 Kakao, Naver, Google Maps API 사용
   * @param {string} address - 변환할 주소
   * @returns {Promise<Object>} 좌표 정보
   */
  async geocodeAddress(address) {
    // TODO: 실제 Geocoding API 구현
    // 현재는 서울시 주요 구 좌표만 제공하는 임시 구현
    const addressCoordinates = {
      '강남구': { latitude: 37.5173, longitude: 127.0473 },
      '강동구': { latitude: 37.5301, longitude: 127.1238 },
      '강북구': { latitude: 37.6397, longitude: 127.0256 },
      '강서구': { latitude: 37.5509, longitude: 126.8495 },
      '관악구': { latitude: 37.4781, longitude: 126.9515 },
      '광진구': { latitude: 37.5385, longitude: 127.0823 },
      '구로구': { latitude: 37.4954, longitude: 126.8874 },
      '금천구': { latitude: 37.4519, longitude: 126.9018 },
      '노원구': { latitude: 37.6542, longitude: 127.0568 },
      '도봉구': { latitude: 37.6688, longitude: 127.0471 },
      '동대문구': { latitude: 37.5744, longitude: 127.0396 },
      '동작구': { latitude: 37.5124, longitude: 126.9393 },
      '마포구': { latitude: 37.5663, longitude: 126.9019 },
      '서대문구': { latitude: 37.5791, longitude: 126.9368 },
      '서초구': { latitude: 37.4837, longitude: 127.0324 },
      '성동구': { latitude: 37.5634, longitude: 127.0371 },
      '성북구': { latitude: 37.5894, longitude: 127.0167 },
      '송파구': { latitude: 37.5145, longitude: 127.1059 },
      '양천구': { latitude: 37.5168, longitude: 126.8665 },
      '영등포구': { latitude: 37.5264, longitude: 126.8962 },
      '용산구': { latitude: 37.5319, longitude: 126.9906 },
      '은평구': { latitude: 37.6026, longitude: 126.9291 },
      '종로구': { latitude: 37.5735, longitude: 126.9788 },
      '중구': { latitude: 37.5640, longitude: 126.9970 },
      '중랑구': { latitude: 37.6063, longitude: 127.0925 }
    };

    // 주소에서 구 이름 추출
    const guMatch = Object.keys(addressCoordinates).find(gu => address.includes(gu));
    
    if (guMatch) {
      return {
        latitude: addressCoordinates[guMatch].latitude,
        longitude: addressCoordinates[guMatch].longitude,
        address: address,
        accuracy: 'district' // 구 단위 정확도
      };
    }

    // 매칭되지 않으면 기본 위치 반환
    return {
      ...DEFAULT_LOCATION,
      address: address,
      accuracy: 'default'
    };
  }

  /**
   * 저장된 위치 정보 조회
   * @returns {Object|null} 저장된 위치 정보
   */
  getSavedLocation() {
    try {
      const saved = localStorage.getItem(LOCATION_STORAGE_KEY);
      if (!saved) return null;

      const location = JSON.parse(saved);
      
      // 위치 정보 만료 확인
      if (Date.now() - location.savedAt > LOCATION_EXPIRY_TIME) {
        this.clearSavedLocation();
        return null;
      }

      return location;
    } catch (error) {
      console.error('Failed to get saved location:', error);
      return null;
    }
  }

  /**
   * 위치 정보를 로컬 스토리지에 저장
   * @param {Object} location - 저장할 위치 정보
   */
  saveLocationToStorage(location) {
    try {
      const locationData = {
        ...location,
        savedAt: Date.now()
      };
      localStorage.setItem(LOCATION_STORAGE_KEY, JSON.stringify(locationData));
    } catch (error) {
      console.error('Failed to save location:', error);
    }
  }

  /**
   * 저장된 위치 정보 삭제
   */
  clearSavedLocation() {
    try {
      localStorage.removeItem(LOCATION_STORAGE_KEY);
      this.currentLocation = null;
    } catch (error) {
      console.error('Failed to clear saved location:', error);
    }
  }

  /**
   * 권한 상태를 로컬 스토리지에 저장
   * @param {string} status - 권한 상태
   */
  savePermissionToStorage(status) {
    try {
      localStorage.setItem(LOCATION_PERMISSION_KEY, status);
    } catch (error) {
      console.error('Failed to save permission status:', error);
    }
  }

  /**
   * 저장된 권한 상태 조회
   * @returns {string|null} 저장된 권한 상태
   */
  getSavedPermissionStatus() {
    try {
      return localStorage.getItem(LOCATION_PERMISSION_KEY);
    } catch (error) {
      console.error('Failed to get saved permission status:', error);
      return null;
    }
  }

  /**
   * Geolocation 에러 처리
   * @param {GeolocationPositionError} error - Geolocation 에러
   * @returns {Error} 처리된 에러 객체
   */
  handleGeolocationError(error) {
    let message;
    let code;

    switch (error.code) {
      case error.PERMISSION_DENIED:
        message = '위치 정보 액세스가 거부되었습니다.';
        code = 'PERMISSION_DENIED';
        break;
      case error.POSITION_UNAVAILABLE:
        message = '위치 정보를 사용할 수 없습니다.';
        code = 'POSITION_UNAVAILABLE';
        break;
      case error.TIMEOUT:
        message = '위치 정보 요청이 시간 초과되었습니다.';
        code = 'TIMEOUT';
        break;
      default:
        message = '위치 정보를 가져오는 중 오류가 발생했습니다.';
        code = 'UNKNOWN_ERROR';
        break;
    }

    const customError = new Error(message);
    customError.code = code;
    customError.originalError = error;
    
    return customError;
  }

  /**
   * 기본 위치 반환
   * @returns {Object} 기본 위치 정보
   */
  getDefaultLocation() {
    return { ...DEFAULT_LOCATION };
  }

  /**
   * 두 좌표 간의 거리 계산 (Haversine 공식)
   * @param {number} lat1 - 첫 번째 위치의 위도
   * @param {number} lon1 - 첫 번째 위치의 경도
   * @param {number} lat2 - 두 번째 위치의 위도
   * @param {number} lon2 - 두 번째 위치의 경도
   * @returns {number} 거리 (km)
   */
  calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // 지구의 반지름 (km)
    const dLat = this.toRad(lat2 - lat1);
    const dLon = this.toRad(lon2 - lon1);
    
    const a = 
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = R * c;
    
    return Math.round(distance * 100) / 100; // 소수점 둘째자리까지
  }

  /**
   * 도를 라디안으로 변환
   * @param {number} deg - 도 단위 각도
   * @returns {number} 라디안 단위 각도
   */
  toRad(deg) {
    return deg * (Math.PI / 180);
  }

  /**
   * 거리를 사용자 친화적 형태로 포맷
   * @param {number} distance - 거리 (km)
   * @returns {string} 포맷된 거리 문자열
   */
  formatDistance(distance) {
    if (distance < 1) {
      return `${Math.round(distance * 1000)}m`;
    } else if (distance < 10) {
      return `${distance.toFixed(1)}km`;
    } else {
      return `${Math.round(distance)}km`;
    }
  }
}

// 싱글톤 인스턴스 생성
export const locationService = new LocationService();

// 편의 함수들
export const getCurrentLocation = () => locationService.getCurrentPosition();
export const checkLocationPermission = () => locationService.checkPermissionStatus();
export const geocodeAddress = (address) => locationService.geocodeAddress(address);
export const getSavedLocation = () => locationService.getSavedLocation();
export const getDefaultLocation = () => locationService.getDefaultLocation(); 