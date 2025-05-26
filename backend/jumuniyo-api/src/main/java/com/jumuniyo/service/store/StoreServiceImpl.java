package com.jumuniyo.service.store;

import com.jumuniyo.config.CacheConfig;
import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import com.jumuniyo.repository.store.CategoryRepository;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.repository.user.UserRepository;
import com.jumuniyo.util.LocationUtils;
import com.jumuniyo.util.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto requestDto, String ownerEmail) {
        log.info("Creating new store: {} for owner: {}", requestDto.getName(), ownerEmail);
        
        // 사용자 조회
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + requestDto.getCategoryId()));
        
        // 사업자 등록번호 중복 체크
        if (storeRepository.existsByBusinessNumber(requestDto.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자 등록번호입니다: " + requestDto.getBusinessNumber());
        }
        
        // DTO를 Entity로 변환
        Store store = requestDto.toEntity(category, owner);
        Store savedStore = storeRepository.save(store);
        
        log.info("Store created successfully with ID: {}", savedStore.getId());
        return StoreResponseDto.from(savedStore);
    }

    @Override
    @Cacheable(
        value = CacheConfig.STORE_CACHE, 
        key = "#storeId", 
        unless = "#result == null"
    )
    public StoreResponseDto getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다: " + storeId));
        
        return StoreResponseDto.from(store);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.STORE_CACHE, key = "#storeId"),
        @CacheEvict(value = CacheConfig.STORE_LIST_CACHE, allEntries = true)
    })
    @Transactional
    public StoreResponseDto updateStore(Long storeId, StoreUpdateRequestDto requestDto, String ownerEmail) {
        Store existingStore = getStore(storeId);
        
        // 사용자 조회
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        // 소유자 권한 체크
        if (!existingStore.isOwnedBy(owner)) {
            throw new IllegalArgumentException("음식점을 수정할 권한이 없습니다.");
        }
        
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + requestDto.getCategoryId()));
        
        // 사업자 등록번호 중복 체크 (현재 음식점 제외)
        if (!existingStore.getBusinessNumber().equals(requestDto.getBusinessNumber()) &&
            storeRepository.existsByBusinessNumberAndIdNot(requestDto.getBusinessNumber(), storeId)) {
            throw new IllegalArgumentException("이미 등록된 사업자 등록번호입니다: " + requestDto.getBusinessNumber());
        }
        
        // 기본 정보 업데이트
        existingStore.updateBasicInfo(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getAddress(),
                requestDto.getPhoneNumber()
        );
        
        // 카테고리 업데이트
        existingStore.updateCategory(category);
        
        // 로고 이미지 업데이트
        if (requestDto.getLogoImageUrl() != null) {
            existingStore.updateLogoImage(requestDto.getLogoImageUrl());
        }
        
        // 배달 정보 업데이트
        existingStore.updateDeliveryInfo(
                requestDto.getMinimumOrderAmount(),
                requestDto.getDeliveryFee()
        );
        
        log.info("Store updated successfully: {}", storeId);
        return StoreResponseDto.from(existingStore);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.STORE_CACHE, key = "#storeId"),
        @CacheEvict(value = CacheConfig.STORE_LIST_CACHE, allEntries = true)
    })
    @Transactional
    public void deleteStore(Long storeId, String ownerEmail) {
        Store store = getStore(storeId);
        
        // 사용자 조회
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        // 소유자 권한 체크
        if (!store.isOwnedBy(owner)) {
            throw new IllegalArgumentException("음식점을 삭제할 권한이 없습니다.");
        }
        
        // 실제 삭제가 아닌 비활성화
        store.deactivate();
        log.info("Store deactivated: {}", storeId);
    }

    @Override
    public Page<StoreResponseDto> getStoresByOwner(String ownerEmail, Pageable pageable) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        Page<Store> stores = storeRepository.findByOwnerAndIsActiveTrue(owner, pageable);
        return stores.map(StoreResponseDto::fromSimple);
    }

    @Override
    public Page<StoreResponseDto> searchStores(String keyword, Long categoryId, String area, 
                                              String sortBy, Boolean approvedOnly, Pageable pageable) {
        // 정렬 조건 설정
        Sort sort = createSort(sortBy);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        
        Page<Store> stores;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색
            stores = storeRepository.searchByKeyword(keyword, sortedPageable);
        } else if (categoryId != null) {
            // 카테고리별 조회
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));
            stores = storeRepository.findByCategoryAndIsActiveTrueAndIsApprovedTrue(category, sortedPageable);
        } else if (area != null && !area.trim().isEmpty()) {
            // 지역별 조회
            stores = storeRepository.findByDeliveryArea(area, sortedPageable);
        } else {
            // 전체 조회
            if (approvedOnly != null && approvedOnly) {
                stores = storeRepository.findByIsActiveTrueAndIsApprovedTrue(sortedPageable);
            } else {
                stores = storeRepository.findByIsActiveTrueAndIsApprovedTrueOrderByRatingDesc(sortedPageable);
            }
        }
        
        return stores.map(StoreResponseDto::fromSimple);
    }

    /**
     * 정렬 조건 생성
     */
    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "reviewCount" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            case "createdAt" -> Sort.by(Sort.Direction.DESC, "createdAt");
            // 새로운 정렬 옵션들 추가
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt"); // 신규매장순
            case "price" -> Sort.by(Sort.Direction.ASC, "minimumOrderAmount"); // 가격순 (낮은 가격부터)
            case "deliveryFee" -> Sort.by(Sort.Direction.ASC, "deliveryFee"); // 배달비순 (낮은 배달비부터)
            case "popular" -> Sort.by(Sort.Direction.DESC, "reviewCount").and(Sort.by(Sort.Direction.DESC, "rating")); // 인기순 (리뷰수+평점)
            // 복합 정렬 조건
            case "recommended" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "reviewCount")); // 추천순 (평점+리뷰수)
            case "alphabetical" -> Sort.by(Sort.Direction.ASC, "name"); // 가나다순
            default -> Sort.by(Sort.Direction.DESC, "rating");
        };
    }

    @Override
    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다: " + storeId));
    }

    @Override
    public List<Store> getStoresByOwner(User owner) {
        return storeRepository.findByOwner(owner);
    }

    @Override
    public Page<Store> getStoresByOwner(User owner, Pageable pageable) {
        return storeRepository.findByOwner(owner, pageable);
    }

    @Override
    @Cacheable(
        value = CacheConfig.STORE_LIST_CACHE,
        key = "'category-' + #categoryId + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize",
        unless = "#result.content.isEmpty()"
    )
    public Page<Store> getStoresByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));
        
        return storeRepository.findByCategoryAndIsActiveTrueAndIsApprovedTrue(category, pageable);
    }

    @Override
    public Page<Store> searchStores(String keyword, Pageable pageable) {
        return storeRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<Store> getStoresByArea(String area, Pageable pageable) {
        return storeRepository.findByDeliveryArea(area, pageable);
    }

    @Override
    public Page<Store> getStoresByRating(Pageable pageable) {
        return storeRepository.findByIsActiveTrueAndIsApprovedTrueOrderByRatingDesc(pageable);
    }

    @Override
    public Page<Store> getStoresByReviewCount(Pageable pageable) {
        return storeRepository.findByIsActiveTrueAndIsApprovedTrueOrderByReviewCountDesc(pageable);
    }

    @Override
    @Transactional
    public Store approveStore(Long storeId) {
        Store store = getStore(storeId);
        store.approve();
        log.info("Store approved: {}", storeId);
        return store;
    }

    @Override
    @Transactional
    public Store rejectStore(Long storeId) {
        Store store = getStore(storeId);
        store.reject();
        log.info("Store rejected: {}", storeId);
        return store;
    }

    @Override
    public Page<Store> getPendingStores(Pageable pageable) {
        return storeRepository.findByIsApprovedFalse(pageable);
    }

    @Override
    public boolean isBusinessNumberDuplicate(String businessNumber) {
        return storeRepository.existsByBusinessNumber(businessNumber);
    }

    @Override
    public boolean isBusinessNumberDuplicate(String businessNumber, Long excludeStoreId) {
        return storeRepository.existsByBusinessNumberAndIdNot(businessNumber, excludeStoreId);
    }

    @Override
    @Transactional
    public Store createStore(Store store, User owner) {
        log.info("Creating new store: {} for owner: {}", store.getName(), owner.getEmail());
        
        // 사업자 등록번호 중복 체크
        if (storeRepository.existsByBusinessNumber(store.getBusinessNumber())) {
            throw new IllegalArgumentException("이미 등록된 사업자 등록번호입니다: " + store.getBusinessNumber());
        }
        
        // Store 엔티티 빌더를 사용하여 새로운 Store 생성
        Store newStore = Store.builder()
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .businessNumber(store.getBusinessNumber())
                .logoImageUrl(store.getLogoImageUrl())
                .minimumOrderAmount(store.getMinimumOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .category(store.getCategory())
                .owner(owner)
                .build();
        
        Store savedStore = storeRepository.save(newStore);
        log.info("Store created successfully with ID: {}", savedStore.getId());
        
        return savedStore;
    }

    @Override
    @Transactional
    public Store updateStore(Long storeId, Store updateData, User currentUser) {
        Store existingStore = getStore(storeId);
        
        // 소유자 권한 체크
        if (!existingStore.isOwnedBy(currentUser)) {
            throw new IllegalArgumentException("음식점을 수정할 권한이 없습니다.");
        }
        
        // 사업자 등록번호 중복 체크 (현재 음식점 제외)
        if (!existingStore.getBusinessNumber().equals(updateData.getBusinessNumber()) &&
            storeRepository.existsByBusinessNumberAndIdNot(updateData.getBusinessNumber(), storeId)) {
            throw new IllegalArgumentException("이미 등록된 사업자 등록번호입니다: " + updateData.getBusinessNumber());
        }
        
        // 기본 정보 업데이트
        existingStore.updateBasicInfo(
                updateData.getName(),
                updateData.getDescription(),
                updateData.getAddress(),
                updateData.getPhoneNumber()
        );
        
        // 카테고리 업데이트
        if (updateData.getCategory() != null) {
            existingStore.updateCategory(updateData.getCategory());
        }
        
        // 로고 이미지 업데이트
        if (updateData.getLogoImageUrl() != null) {
            existingStore.updateLogoImage(updateData.getLogoImageUrl());
        }
        
        // 배달 정보 업데이트
        existingStore.updateDeliveryInfo(
                updateData.getMinimumOrderAmount(),
                updateData.getDeliveryFee()
        );
        
        log.info("Store updated successfully: {}", storeId);
        return existingStore;
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId, User currentUser) {
        Store store = getStore(storeId);
        
        // 소유자 권한 체크
        if (!store.isOwnedBy(currentUser)) {
            throw new IllegalArgumentException("음식점을 삭제할 권한이 없습니다.");
        }
        
        // 실제 삭제가 아닌 비활성화
        store.deactivate();
        log.info("Store deactivated: {}", storeId);
    }

    @Override
    @Transactional
    public StoreResponseDto toggleStoreStatus(Long storeId, String ownerEmail) {
        Store store = getStore(storeId);
        
        // 사용자 조회
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        // 소유자 권한 체크
        if (!store.isOwnedBy(owner)) {
            throw new IllegalArgumentException("음식점 상태를 변경할 권한이 없습니다.");
        }
        
        // 상태 토글
        if (store.getIsActive()) {
            store.deactivate();
            log.info("Store deactivated: {}", storeId);
        } else {
            store.activate();
            log.info("Store activated: {}", storeId);
        }
        
        return StoreResponseDto.from(store);
    }

    @Override
    @Transactional
    public StoreResponseDto updateStoreLogo(Long storeId, String imageUrl, Long ownerId) {
        log.info("Updating store logo: storeId={}, ownerId={}", storeId, ownerId);
        
        Store store = getStore(storeId);
        
        // 소유자 권한 체크
        if (!store.isOwnedBy(ownerId)) {
            throw new IllegalArgumentException("가게 로고를 수정할 권한이 없습니다.");
        }
        
        // 로고 이미지 업데이트
        store.updateLogoImage(imageUrl);
        
        log.info("Store logo updated successfully: storeId={}", storeId);
        return StoreResponseDto.from(store);
    }

    @Override
    @Transactional
    public void deleteStoreLogo(Long storeId, Long ownerId) {
        log.info("Deleting store logo: storeId={}, ownerId={}", storeId, ownerId);
        
        Store store = getStore(storeId);
        
        // 소유자 권한 체크
        if (!store.isOwnedBy(ownerId)) {
            throw new IllegalArgumentException("가게 로고를 삭제할 권한이 없습니다.");
        }
        
        // 기존 로고 이미지 URL 가져오기
        String currentLogoUrl = store.getLogoImageUrl();
        
        // 로고 이미지 제거
        store.updateLogoImage(null);
        
        log.info("Store logo deleted successfully: storeId={}", storeId);
    }

    @Override
    public Page<StoreResponseDto> searchStoresWithFilters(String keyword, Long categoryId, String area, 
                                                         String sortBy, Boolean approvedOnly,
                                                         BigDecimal minRating, BigDecimal maxMinimumOrderAmount, 
                                                         BigDecimal maxDeliveryFee, Integer maxDeliveryTime,
                                                         Integer minReviewCount, Boolean freeDeliveryOnly, 
                                                         Boolean newStoreOnly, Pageable pageable) {
        
        log.debug("필터링 옵션이 포함된 음식점 검색 실행: keyword={}, categoryId={}, minRating={}, maxDeliveryFee={}", 
                 keyword, categoryId, minRating, maxDeliveryFee);
        
        Page<Store> stores;
        
        // 검색 조건에 따라 적절한 Repository 메서드 호출
        if (keyword != null && !keyword.trim().isEmpty()) {
            stores = storeRepository.searchByKeywordWithFilters(
                    keyword, minRating, maxMinimumOrderAmount, maxDeliveryFee, 
                    minReviewCount, freeDeliveryOnly, pageable);
        } else if (categoryId != null) {
            stores = storeRepository.findByCategoryWithFilters(
                    categoryId, minRating, maxMinimumOrderAmount, maxDeliveryFee, 
                    minReviewCount, freeDeliveryOnly, pageable);
        } else if (area != null && !area.trim().isEmpty()) {
            stores = storeRepository.findByDeliveryAreaWithFilters(
                    area, minRating, maxMinimumOrderAmount, maxDeliveryFee, 
                    maxDeliveryTime, minReviewCount, freeDeliveryOnly, pageable);
        } else {
            stores = storeRepository.findAllWithFilters(
                    minRating, maxMinimumOrderAmount, maxDeliveryFee, 
                    minReviewCount, freeDeliveryOnly, pageable);
        }
        
        // 신규 매장 필터링 적용 (생성일 기준으로 최근 한 달 이내)
        if (newStoreOnly != null && newStoreOnly) {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            List<Store> filteredStores = stores.getContent().stream()
                    .filter(store -> store.getCreatedAt().isAfter(oneMonthAgo))
                    .collect(Collectors.toList());
            stores = new PageImpl<>(filteredStores, pageable, filteredStores.size());
        }
        
        // Store 엔티티를 StoreResponseDto로 변환
        List<StoreResponseDto> storeDtos = stores.getContent().stream()
                .map(StoreResponseDto::fromSimple)
                .collect(Collectors.toList());
        
        return new PageImpl<>(storeDtos, pageable, stores.getTotalElements());
    }

    @Override
    public Page<StoreResponseDto> searchStoresWithLocationAndFilters(String keyword, Long categoryId, String area, 
                                                                    BigDecimal latitude, BigDecimal longitude, 
                                                                    Double radiusKm, String sortBy, Boolean approvedOnly,
                                                                    BigDecimal minRating, BigDecimal maxMinimumOrderAmount, 
                                                                    BigDecimal maxDeliveryFee, Integer maxDeliveryTime,
                                                                    Integer minReviewCount, Boolean freeDeliveryOnly, 
                                                                    Boolean newStoreOnly, Pageable pageable) {
        
        log.debug("위치 기반 + 필터링 음식점 검색 실행: lat={}, lng={}, radius={}, keyword={}, categoryId={}", 
                 latitude, longitude, radiusKm, keyword, categoryId);
        
        // 좌표 유효성 검사
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("위도와 경도는 필수 입력값입니다.");
        }
        
        if (latitude.compareTo(new BigDecimal("-90")) < 0 || latitude.compareTo(new BigDecimal("90")) > 0) {
            throw new IllegalArgumentException("위도는 -90도와 90도 사이의 값이어야 합니다.");
        }
        
        if (longitude.compareTo(new BigDecimal("-180")) < 0 || longitude.compareTo(new BigDecimal("180")) > 0) {
            throw new IllegalArgumentException("경도는 -180도와 180도 사이의 값이어야 합니다.");
        }
        
        // 검색 반경 설정 (기본값: 10km, 최대: 50km)
        double searchRadiusKm = radiusKm != null ? radiusKm : 10.0;
        if (searchRadiusKm <= 0 || searchRadiusKm > 50) {
            searchRadiusKm = 10.0;
        }
        
        // 후보 음식점 조회 (위치 정보가 있는 음식점만)
        List<Store> candidateStores;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            candidateStores = storeRepository.searchByKeywordWithLocation(keyword);
        } else if (categoryId != null) {
            candidateStores = storeRepository.findByCategoryWithLocation(categoryId);
        } else if (area != null && !area.trim().isEmpty()) {
            candidateStores = storeRepository.findByDeliveryAreaWithLocation(area);
        } else {
            candidateStores = storeRepository.findActiveStoresWithLocation();
        }
        
        final double finalSearchRadiusKm = searchRadiusKm;
        
        // 거리 계산 및 반경 내 필터링 + 기타 필터링 조건 적용
        List<StoreWithDistance> storesWithDistance = candidateStores.stream()
                .map(store -> {
                    BigDecimal distance = LocationUtils.calculateDistanceAsBigDecimal(
                            latitude, longitude, 
                            store.getLatitude(), store.getLongitude()
                    );
                    return new StoreWithDistance(store, distance);
                })
                .filter(swd -> swd.getDistance().doubleValue() <= finalSearchRadiusKm)
                // 필터링 조건 적용
                .filter(swd -> {
                    Store store = swd.getStore();
                    
                    // 평점 필터
                    if (minRating != null && store.getRating().compareTo(minRating) < 0) {
                        return false;
                    }
                    
                    // 최소주문금액 필터
                    if (maxMinimumOrderAmount != null && store.getMinimumOrderAmount().compareTo(maxMinimumOrderAmount) > 0) {
                        return false;
                    }
                    
                    // 배달비 필터
                    if (maxDeliveryFee != null && store.getDeliveryFee().compareTo(maxDeliveryFee) > 0) {
                        return false;
                    }
                    
                    // 리뷰수 필터
                    if (minReviewCount != null && store.getReviewCount() < minReviewCount) {
                        return false;
                    }
                    
                    // 무료배달 필터
                    if (freeDeliveryOnly != null && freeDeliveryOnly && store.getDeliveryFee().compareTo(BigDecimal.ZERO) > 0) {
                        return false;
                    }
                    
                    // 신규매장 필터 (생성일 기준으로 최근 한 달 이내)
                    if (newStoreOnly != null && newStoreOnly) {
                        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
                        if (store.getCreatedAt().isBefore(oneMonthAgo)) {
                            return false;
                        }
                    }
                    
                    // 배달시간 필터 (지역별 배달시간을 고려)
                    if (maxDeliveryTime != null) {
                        boolean hasValidDeliveryTime = store.getDeliveryAreas().stream()
                                .anyMatch(da -> da.getIsActive() && da.getDeliveryTimeMinutes() <= maxDeliveryTime);
                        if (!hasValidDeliveryTime) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        // 정렬 적용
        sortStoresWithDistance(storesWithDistance, sortBy);
        
        // 성능 최적화된 페이징 처리
        long startTime = System.currentTimeMillis();
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        
        // StoreResponseDto로 변환 후 페이징 처리
        List<StoreResponseDto> allResults = storesWithDistance.stream()
                .map(swd -> StoreResponseDto.fromSimpleWithDistance(swd.getStore(), swd.getDistance()))
                .collect(Collectors.toList());
        
        Page<StoreResponseDto> result = PaginationUtils.createPageFromList(allResults, validatedPageable);
        
        // 성능 통계 로깅
        long processingTime = System.currentTimeMillis() - startTime;
        PaginationUtils.logPaginationStats("searchStoresWithLocationAndFilters", allResults.size(), validatedPageable, processingTime);
        
        return result;
    }

    @Override
    public Page<StoreResponseDto> searchStoresWithLocation(String keyword, Long categoryId, String area, 
                                                          BigDecimal latitude, BigDecimal longitude, 
                                                          Double radiusKm, String sortBy, Boolean approvedOnly, 
                                                          Pageable pageable) {
        log.info("위치 기반 음식점 검색: keyword={}, categoryId={}, area={}, lat={}, lon={}, radius={}km", 
                keyword, categoryId, area, latitude, longitude, radiusKm);
        
        // 위치 정보 유효성 검사
        if (latitude != null && longitude != null && !LocationUtils.isValidCoordinates(latitude, longitude)) {
            throw new IllegalArgumentException("유효하지 않은 위치 정보입니다.");
        }
        
        // 반경 기본값 설정
        double searchRadius = radiusKm != null ? radiusKm : LocationUtils.DEFAULT_SEARCH_RADIUS_KM;
        if (searchRadius > LocationUtils.MAX_SEARCH_RADIUS_KM) {
            searchRadius = LocationUtils.MAX_SEARCH_RADIUS_KM;
        }
        
        List<Store> candidateStores = new ArrayList<>();
        
        // 검색 조건에 따라 후보 음식점 목록 조회
        if (keyword != null && !keyword.trim().isEmpty() && categoryId != null) {
            // 키워드 + 카테고리 검색
            candidateStores = storeRepository.findByCategoryAndKeywordWithLocation(categoryId, keyword);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색
            candidateStores = storeRepository.searchByKeywordWithLocation(keyword);
        } else if (categoryId != null) {
            // 카테고리별 조회
            candidateStores = storeRepository.findByCategoryWithLocation(categoryId);
        } else if (area != null && !area.trim().isEmpty()) {
            // 지역별 조회
            candidateStores = storeRepository.findByDeliveryAreaWithLocation(area);
        } else {
            // 전체 조회
            candidateStores = storeRepository.findActiveStoresWithLocation();
        }
        
        // 위치 기반 필터링 및 거리 계산
        List<StoreWithDistance> storesWithDistance = new ArrayList<>();
        
        for (Store store : candidateStores) {
            BigDecimal distance = null;
            
            if (latitude != null && longitude != null && 
                store.getLatitude() != null && store.getLongitude() != null) {
                
                // 거리 계산
                distance = LocationUtils.calculateDistanceAsBigDecimal(
                    latitude, longitude, store.getLatitude(), store.getLongitude());
                
                // 반경 내 음식점만 포함
                if (distance.doubleValue() <= searchRadius) {
                    storesWithDistance.add(new StoreWithDistance(store, distance));
                }
            } else if (latitude == null || longitude == null) {
                // 사용자 위치가 없으면 거리 관계없이 포함
                storesWithDistance.add(new StoreWithDistance(store, null));
            }
        }
        
        // 정렬
        sortStoresWithDistance(storesWithDistance, sortBy);
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), storesWithDistance.size());
        
        List<StoreWithDistance> pagedStores = storesWithDistance.subList(start, end);
        
        // DTO 변환
        List<StoreResponseDto> storeDtos = pagedStores.stream()
                .map(swd -> swd.getDistance() != null ? 
                    StoreResponseDto.fromSimpleWithDistance(swd.getStore(), swd.getDistance()) :
                    StoreResponseDto.fromSimple(swd.getStore()))
                .collect(Collectors.toList());
        
        return new PageImpl<>(storeDtos, pageable, storesWithDistance.size());
    }
    
    /**
     * 거리 정보를 포함한 음식점 정렬
     */
    private void sortStoresWithDistance(List<StoreWithDistance> storesWithDistance, String sortBy) {
        Comparator<StoreWithDistance> comparator = switch (sortBy) {
            case "distance" -> Comparator.comparing(swd -> swd.getDistance() != null ? swd.getDistance() : BigDecimal.valueOf(Double.MAX_VALUE));
            case "rating" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getRating()).reversed();
            case "reviewCount" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getReviewCount()).reversed();
            case "name" -> Comparator.comparing(swd -> swd.getStore().getName());
            case "createdAt" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getCreatedAt()).reversed();
            // 새로운 정렬 옵션들 추가
            case "newest" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getCreatedAt()).reversed(); // 신규매장순
            case "price" -> Comparator.comparing(swd -> swd.getStore().getMinimumOrderAmount()); // 가격순 (낮은 가격부터)
            case "deliveryFee" -> Comparator.comparing(swd -> swd.getStore().getDeliveryFee()); // 배달비순 (낮은 배달비부터)
            case "popular" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getReviewCount()).reversed()
                    .thenComparing((StoreWithDistance swd) -> swd.getStore().getRating()).reversed(); // 인기순 (리뷰수+평점)
            case "recommended" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getRating()).reversed()
                    .thenComparing((StoreWithDistance swd) -> swd.getStore().getReviewCount()).reversed(); // 추천순 (평점+리뷰수)
            case "alphabetical" -> Comparator.comparing(swd -> swd.getStore().getName()); // 가나다순
            // 거리 기반 복합 정렬
            case "nearbyRating" -> Comparator.comparing((StoreWithDistance swd) -> swd.getDistance() != null ? swd.getDistance() : BigDecimal.valueOf(Double.MAX_VALUE))
                    .thenComparing((StoreWithDistance swd) -> swd.getStore().getRating()).reversed(); // 가까운 거리 + 높은 평점
            case "ratingNearby" -> Comparator.comparing((StoreWithDistance swd) -> swd.getStore().getRating()).reversed()
                    .thenComparing(swd -> swd.getDistance() != null ? swd.getDistance() : BigDecimal.valueOf(Double.MAX_VALUE)); // 높은 평점 + 가까운 거리
            default -> {
                // 기본값: 거리순 정렬, 거리가 없으면 평점순
                yield Comparator.comparing((StoreWithDistance swd) -> swd.getDistance() != null ? swd.getDistance() : BigDecimal.valueOf(Double.MAX_VALUE))
                        .thenComparing((StoreWithDistance swd) -> swd.getStore().getRating(), Comparator.reverseOrder());
            }
        };
        
        storesWithDistance.sort(comparator);
    }
    
    /**
     * 음식점과 거리 정보를 함께 저장하는 내부 클래스
     */
    private static class StoreWithDistance {
        private final Store store;
        private final BigDecimal distance;
        
        public StoreWithDistance(Store store, BigDecimal distance) {
            this.store = store;
            this.distance = distance;
        }
        
        public Store getStore() {
            return store;
        }
        
        public BigDecimal getDistance() {
            return distance;
        }
    }

    @Override
    @Cacheable(
        value = CacheConfig.STORE_LIST_CACHE,
        key = "'radius-' + #latitude.toString() + '-' + #longitude.toString() + '-' + #radiusKm + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize",
        unless = "#result.content.isEmpty()"
    )
    public Page<StoreResponseDto> findStoresWithinRadius(
            BigDecimal latitude, 
            BigDecimal longitude, 
            Double radiusKm, 
            Pageable pageable) {
        
        log.debug("최적화된 위치 기반 검색 실행: lat={}, lng={}, radius={}km", latitude, longitude, radiusKm);
        
        // 위치 정보 유효성 검사
        validateCoordinates(latitude, longitude);
        
        // 검색 반경 설정 및 검증
        double searchRadiusKm = validateAndAdjustRadius(radiusKm);
        
        // 미터 단위로 변환 (ST_Distance_Sphere는 미터 단위 사용)
        double radiusMeters = searchRadiusKm * 1000;
        
        // 페이지네이션 정보 추출
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        int offset = (int) validatedPageable.getOffset();
        int limit = validatedPageable.getPageSize();
        
        // 성능 측정 시작
        long startTime = System.currentTimeMillis();
        
        // 공간 쿼리 실행
        List<Store> stores = storeRepository.findStoresWithinRadiusPaged(
                latitude, longitude, radiusMeters, offset, limit);
        
        // 총 개수 조회 (별도 쿼리)
        long totalElements = storeRepository.countStoresWithinRadius(
                latitude, longitude, radiusMeters);
        
        // DTO 변환
        List<StoreResponseDto> storeResponseDtos = stores.stream()
                .map(store -> {
                    // 거리 계산 (이미 DB에서 필터링되었으므로 여기서는 정확한 거리만 계산)
                    BigDecimal distance = LocationUtils.calculateDistanceAsBigDecimal(
                            latitude, longitude, store.getLatitude(), store.getLongitude());
                    return StoreResponseDto.fromSimpleWithDistance(store, distance);
                })
                .collect(Collectors.toList());
        
        // 페이지 객체 생성
        Page<StoreResponseDto> result = new PageImpl<>(
                storeResponseDtos, validatedPageable, totalElements);
        
        // 성능 통계 로깅
        long processingTime = System.currentTimeMillis() - startTime;
        PaginationUtils.logPaginationStats(
                "findStoresWithinRadius", stores.size(), validatedPageable, processingTime);
        
        return result;
    }

    @Override
    @Cacheable(
        value = CacheConfig.STORE_LIST_CACHE,
        key = "'radius-filter-' + #latitude.toString() + '-' + #longitude.toString() + '-' + #radiusKm + '-' + #keyword + '-' + #sortBy + '-page-' + #pageable.pageNumber + '-size-' + #pageable.pageSize",
        unless = "#result.content.isEmpty()"
    )
    public Page<StoreResponseDto> findStoresWithinRadiusWithFilters(
            BigDecimal latitude, 
            BigDecimal longitude, 
            Double radiusKm, 
            String keyword,
            BigDecimal minRating,
            BigDecimal maxDeliveryFee,
            Integer minReviewCount,
            Boolean freeDeliveryOnly,
            String sortBy,
            Pageable pageable) {
        
        log.debug("최적화된 위치 기반 필터링 검색 실행: lat={}, lng={}, radius={}km, keyword={}", 
                 latitude, longitude, radiusKm, keyword);
        
        // 위치 정보 유효성 검사
        validateCoordinates(latitude, longitude);
        
        // 검색 반경 설정 및 검증
        double searchRadiusKm = validateAndAdjustRadius(radiusKm);
        
        // 미터 단위로 변환
        double radiusMeters = searchRadiusKm * 1000;
        
        // 페이지네이션 정보 추출
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        int offset = (int) validatedPageable.getOffset();
        int limit = validatedPageable.getPageSize();
        
        // 정렬 옵션 기본값 설정
        String effectiveSortBy = (sortBy != null) ? sortBy : "distance";
        
        // 키워드 정규화
        String effectiveKeyword = (keyword != null) ? keyword : "";
        
        // 필터링 기본값 설정
        Boolean effectiveFreeDeliveryOnly = (freeDeliveryOnly != null) ? freeDeliveryOnly : false;
        
        // 성능 측정 시작
        long startTime = System.currentTimeMillis();
        
        // 공간 쿼리 실행 (필터링 포함)
        List<Store> stores = storeRepository.findStoresWithinRadiusWithFilters(
                latitude, longitude, radiusMeters, 
                effectiveKeyword, minRating, maxDeliveryFee, 
                minReviewCount, effectiveFreeDeliveryOnly,
                effectiveSortBy, offset, limit);
        
        // 총 개수 조회 (별도 쿼리)
        long totalElements = storeRepository.countStoresWithinRadiusWithFilters(
                latitude, longitude, radiusMeters,
                effectiveKeyword, minRating, maxDeliveryFee,
                minReviewCount, effectiveFreeDeliveryOnly);
        
        // DTO 변환
        List<StoreResponseDto> storeResponseDtos = stores.stream()
                .map(store -> {
                    // 거리 계산
                    BigDecimal distance = LocationUtils.calculateDistanceAsBigDecimal(
                            latitude, longitude, store.getLatitude(), store.getLongitude());
                    return StoreResponseDto.fromSimpleWithDistance(store, distance);
                })
                .collect(Collectors.toList());
        
        // 페이지 객체 생성
        Page<StoreResponseDto> result = new PageImpl<>(
                storeResponseDtos, validatedPageable, totalElements);
        
        // 성능 통계 로깅
        long processingTime = System.currentTimeMillis() - startTime;
        PaginationUtils.logPaginationStats(
                "findStoresWithinRadiusWithFilters", stores.size(), validatedPageable, processingTime);
        
        return result;
    }
    
    /**
     * 위치 좌표 유효성 검사
     */
    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("위도와 경도는 필수 입력값입니다.");
        }
        
        if (!LocationUtils.isValidLatitude(latitude)) {
            throw new IllegalArgumentException("위도는 -90도와 90도 사이의 값이어야 합니다.");
        }
        
        if (!LocationUtils.isValidLongitude(longitude)) {
            throw new IllegalArgumentException("경도는 -180도와 180도 사이의 값이어야 합니다.");
        }
    }
    
    /**
     * 검색 반경 검증 및 조정
     */
    private double validateAndAdjustRadius(Double radiusKm) {
        // 기본값 설정
        double searchRadius = radiusKm != null ? radiusKm : LocationUtils.DEFAULT_SEARCH_RADIUS_KM;
        
        // 범위 검증
        if (searchRadius <= 0) {
            searchRadius = LocationUtils.DEFAULT_SEARCH_RADIUS_KM;
            log.warn("Invalid search radius ({}km), using default: {}km", radiusKm, searchRadius);
        } else if (searchRadius > LocationUtils.MAX_SEARCH_RADIUS_KM) {
            searchRadius = LocationUtils.MAX_SEARCH_RADIUS_KM;
            log.warn("Search radius too large ({}km), capped at: {}km", radiusKm, searchRadius);
        }
        
        return searchRadius;
    }
} 