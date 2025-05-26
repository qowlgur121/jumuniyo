package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
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
     * 위치 기반 음식점 검색 (DTO 기반)
     */
    Page<StoreResponseDto> searchStoresWithLocation(String keyword, Long categoryId, String area, 
                                                   BigDecimal latitude, BigDecimal longitude, 
                                                   Double radiusKm, String sortBy, Boolean approvedOnly, 
                                                   Pageable pageable);

    /**
     * 필터링이 포함된 음식점 검색 (DTO 기반)
     */
    Page<StoreResponseDto> searchStoresWithFilters(String keyword, Long categoryId, String area, 
                                                  String sortBy, Boolean approvedOnly,
                                                  BigDecimal minRating, BigDecimal maxMinimumOrderAmount, 
                                                  BigDecimal maxDeliveryFee, Integer maxDeliveryTime,
                                                  Integer minReviewCount, Boolean freeDeliveryOnly, 
                                                  Boolean newStoreOnly, Pageable pageable);

    /**
     * 위치 기반 + 필터링이 포함된 음식점 검색 (DTO 기반)
     */
    Page<StoreResponseDto> searchStoresWithLocationAndFilters(String keyword, Long categoryId, String area, 
                                                             BigDecimal latitude, BigDecimal longitude, 
                                                             Double radiusKm, String sortBy, Boolean approvedOnly,
                                                             BigDecimal minRating, BigDecimal maxMinimumOrderAmount, 
                                                             BigDecimal maxDeliveryFee, Integer maxDeliveryTime,
                                                             Integer minReviewCount, Boolean freeDeliveryOnly, 
                                                             Boolean newStoreOnly, Pageable pageable);

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

    /**
     * 음식점 영업 상태 토글
     */
    StoreResponseDto toggleStoreStatus(Long storeId, String ownerEmail);

    /**
     * 가게 로고 이미지 업데이트
     */
    StoreResponseDto updateStoreLogo(Long storeId, String imageUrl, Long ownerId);

    /**
     * 가게 로고 이미지 삭제
     */
    void deleteStoreLogo(Long storeId, Long ownerId);

    /**
     * 최적화된 위치 기반 검색 - 공간 인덱스 활용
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @param radiusKm 검색 반경 (킬로미터)
     * @param pageable 페이징 정보
     * @return 페이징된 음식점 목록
     */
    Page<StoreResponseDto> findStoresWithinRadius(
            BigDecimal latitude, 
            BigDecimal longitude, 
            Double radiusKm, 
            Pageable pageable);

    /**
     * 최적화된 위치 기반 검색 (필터링 포함) - 공간 인덱스 활용
     * 
     * @param latitude 위도
     * @param longitude 경도
     * @param radiusKm 검색 반경 (킬로미터)
     * @param keyword 검색 키워드
     * @param minRating 최소 평점
     * @param maxDeliveryFee 최대 배달비
     * @param minReviewCount 최소 리뷰 수
     * @param freeDeliveryOnly 무료 배달만 검색
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 페이징된 음식점 목록
     */
    Page<StoreResponseDto> findStoresWithinRadiusWithFilters(
            BigDecimal latitude, 
            BigDecimal longitude, 
            Double radiusKm, 
            String keyword,
            BigDecimal minRating,
            BigDecimal maxDeliveryFee,
            Integer minReviewCount,
            Boolean freeDeliveryOnly,
            String sortBy,
            Pageable pageable);
} 