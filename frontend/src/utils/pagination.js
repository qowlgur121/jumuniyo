/**
 * 페이지네이션 관련 유틸리티 함수들
 */

/**
 * 페이지네이션 파라미터를 검증하고 안전한 값으로 조정
 * @param {Object} params - 페이지네이션 파라미터 객체
 * @param {number} params.page - 페이지 번호 (0부터 시작)
 * @param {number} params.size - 페이지 크기
 * @returns {Object} 검증된 페이지네이션 파라미터
 */
export const validatePaginationParams = (params = {}) => {
  const { page = 0, size = 20 } = params;
  
  return {
    page: Math.max(0, Math.min(page, 1000)), // 페이지 번호: 0-1000
    size: Math.max(1, Math.min(size, 100))   // 페이지 크기: 1-100
  };
};

/**
 * 페이지네이션 정보를 기반으로 전체 페이지 번호 배열 생성
 * @param {Object} pagination - 페이지네이션 메타데이터
 * @param {number} maxVisible - 최대 표시할 페이지 번호 개수
 * @returns {Array<number>} 페이지 번호 배열
 */
export const generatePageNumbers = (pagination, maxVisible = 5) => {
  if (!pagination || pagination.totalPages <= 1) {
    return [0];
  }
  
  const { page: currentPage, totalPages } = pagination;
  const pages = [];
  
  // 전체 페이지가 maxVisible보다 적은 경우
  if (totalPages <= maxVisible) {
    for (let i = 0; i < totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }
  
  // 현재 페이지를 중심으로 페이지 번호 생성
  const half = Math.floor(maxVisible / 2);
  let start = Math.max(0, currentPage - half);
  let end = Math.min(totalPages - 1, start + maxVisible - 1);
  
  // 끝부분에 맞추어 시작점 조정
  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1);
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  
  return pages;
};

/**
 * 페이지네이션 표시를 위한 UI 정보 생성
 * @param {Object} pagination - 페이지네이션 메타데이터
 * @param {number} maxVisible - 최대 표시할 페이지 번호 개수
 * @returns {Object} UI 페이지네이션 정보
 */
export const createPaginationInfo = (pagination, maxVisible = 5) => {
  if (!pagination) {
    return {
      pages: [],
      hasPrevious: false,
      hasNext: false,
      showFirstPage: false,
      showLastPage: false,
      totalInfo: '0개의 결과'
    };
  }
  
  const { page, totalPages, totalElements, numberOfElements } = pagination;
  const pages = generatePageNumbers(pagination, maxVisible);
  
  return {
    pages,
    hasPrevious: page > 0,
    hasNext: page < totalPages - 1,
    showFirstPage: pages[0] > 0,
    showLastPage: pages[pages.length - 1] < totalPages - 1,
    totalInfo: `총 ${totalElements.toLocaleString()}개 중 ${numberOfElements}개 표시`
  };
};

/**
 * 페이지 크기 옵션 생성
 * @returns {Array<Object>} 페이지 크기 옵션 배열
 */
export const getPageSizeOptions = () => [
  { value: 10, label: '10개씩 보기' },
  { value: 20, label: '20개씩 보기' },
  { value: 50, label: '50개씩 보기' },
  { value: 100, label: '100개씩 보기' }
];

/**
 * URL 쿼리 파라미터에서 페이지네이션 정보 추출
 * @param {URLSearchParams} searchParams - URL 검색 파라미터
 * @returns {Object} 페이지네이션 파라미터
 */
export const extractPaginationFromUrl = (searchParams) => {
  const page = parseInt(searchParams.get('page')) || 0;
  const size = parseInt(searchParams.get('size')) || 20;
  
  return validatePaginationParams({ page, size });
};

/**
 * 페이지네이션 파라미터를 URL 쿼리 문자열로 변환
 * @param {Object} params - 페이지네이션 파라미터
 * @param {Object} additionalParams - 추가 파라미터
 * @returns {string} 쿼리 문자열
 */
export const buildPaginationQuery = (params, additionalParams = {}) => {
  const validatedParams = validatePaginationParams(params);
  const allParams = { ...additionalParams, ...validatedParams };
  
  // null이나 undefined 값 제거
  const filteredParams = Object.entries(allParams)
    .filter(([_, value]) => value !== null && value !== undefined && value !== '')
    .reduce((acc, [key, value]) => ({ ...acc, [key]: value }), {});
  
  return new URLSearchParams(filteredParams).toString();
};

/**
 * 무한 스크롤을 위한 페이지 로드 체크
 * @param {Element} element - 스크롤 대상 엘리먼트
 * @param {number} threshold - 로드 임계값 (px)
 * @returns {boolean} 다음 페이지 로드 여부
 */
export const shouldLoadNextPage = (element, threshold = 200) => {
  if (!element) return false;
  
  const { scrollTop, scrollHeight, clientHeight } = element;
  return scrollHeight - scrollTop - clientHeight < threshold;
};

/**
 * 페이지네이션 성능 최적화를 위한 디바운스 함수
 * @param {Function} func - 실행할 함수
 * @param {number} delay - 지연 시간 (ms)
 * @returns {Function} 디바운스된 함수
 */
export const debouncePagination = (func, delay = 300) => {
  let timeoutId;
  
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func.apply(null, args), delay);
  };
};

/**
 * 페이지네이션 상태 관리를 위한 커스텀 훅 (Composition API용)
 * @param {Object} initialParams - 초기 페이지네이션 파라미터
 * @returns {Object} 페이지네이션 상태 및 제어 함수들
 */
export const usePagination = (initialParams = {}) => {
  const params = reactive(validatePaginationParams(initialParams));
  const loading = ref(false);
  const data = ref([]);
  const pagination = ref(null);
  
  const goToPage = (page) => {
    params.page = Math.max(0, page);
  };
  
  const changePageSize = (size) => {
    params.size = size;
    params.page = 0; // 페이지 크기 변경 시 첫 페이지로
  };
  
  const nextPage = () => {
    if (pagination.value && !pagination.value.last) {
      goToPage(params.page + 1);
    }
  };
  
  const previousPage = () => {
    if (pagination.value && !pagination.value.first) {
      goToPage(params.page - 1);
    }
  };
  
  const reset = () => {
    params.page = 0;
    data.value = [];
    pagination.value = null;
  };
  
  return {
    params,
    loading,
    data,
    pagination,
    goToPage,
    changePageSize,
    nextPage,
    previousPage,
    reset
  };
}; 