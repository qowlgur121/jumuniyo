package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreService {

    /**
     * 음식점 등록 (DTO 기반)
     */
    StoreResponseDto createStore(StoreCreateRequestDto requestDto, String ownerEmail);

    /**
     * 음식점 등록 (Entity 기반)
     */
    Store createStore(Store store, User owner);

    /**
     * 음식점 정보 조회 (DTO 기반)
     */
    StoreResponseDto getStoreById(Long storeId);

    /**
     * 음식점 정보 조회 (Entity 기반)
     */
    Store getStore(Long storeId);

    /**
     * 음식점 정보 수정 (DTO 기반)
     */
    StoreResponseDto updateStore(Long storeId, StoreUpdateRequestDto requestDto, String ownerEmail);

    /**
     * 음식점 정보 수정 (Entity 기반)
     */
    Store updateStore(Long storeId, Store updateData, User currentUser);

    /**
     * 음식점 삭제 (비활성화) - DTO 기반
     */
    void deleteStore(Long storeId, String ownerEmail);

    /**
     * 음식점 삭제 (비활성화) - Entity 기반
     */
    void deleteStore(Long storeId, User currentUser);

    /**
     * 사장님이 소유한 음식점 목록 조회 (DTO 기반)
     */
    Page<StoreResponseDto> getStoresByOwner(String ownerEmail, Pageable pageable);

    /**
     * 사장님이 소유한 음식점 목록 조회 (Entity 기반)
     */
    List<Store> getStoresByOwner(User owner);

    /**
     * 사장님이 소유한 음식점 목록 조회 (페이징)
     */
    Page<Store> getStoresByOwner(User owner, Pageable pageable);

    /**
     * 음식점 검색 (DTO 기반)
     */
    Page<StoreResponseDto> searchStores(String keyword, Long categoryId, String area, 
                                       String sortBy, Boolean approvedOnly, Pageable pageable);

    /**
     * 카테고리별 음식점 조회
     */
    Page<Store> getStoresByCategory(Long categoryId, Pageable pageable);

    /**
     * 음식점 검색 (Entity 기반)
     */
    Page<Store> searchStores(String keyword, Pageable pageable);

    /**
     * 지역별 음식점 조회
     */
    Page<Store> getStoresByArea(String area, Pageable pageable);

    /**
     * 평점 순 음식점 조회
     */
    Page<Store> getStoresByRating(Pageable pageable);

    /**
     * 리뷰 수 순 음식점 조회
     */
    Page<Store> getStoresByReviewCount(Pageable pageable);

    /**
     * 음식점 승인 (관리자용)
     */
    Store approveStore(Long storeId);

    /**
     * 음식점 승인 거부 (관리자용)
     */
    Store rejectStore(Long storeId);

    /**
     * 승인 대기 중인 음식점 조회 (관리자용)
     */
    Page<Store> getPendingStores(Pageable pageable);

    /**
     * 사업자 등록번호 중복 체크
     */
    boolean isBusinessNumberDuplicate(String businessNumber);

    /**
     * 사업자 등록번호 중복 체크 (수정 시)
     */
    boolean isBusinessNumberDuplicate(String businessNumber, Long excludeStoreId);
} 