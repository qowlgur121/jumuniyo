package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import com.jumuniyo.repository.store.CategoryRepository;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public StoreResponseDto getStoreById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다: " + storeId));
        
        return StoreResponseDto.from(store);
    }

    @Override
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
        
        Page<Store> stores = storeRepository.findByOwner(owner, pageable);
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

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "reviewCount" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            case "createdAt" -> Sort.by(Sort.Direction.DESC, "createdAt");
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
} 