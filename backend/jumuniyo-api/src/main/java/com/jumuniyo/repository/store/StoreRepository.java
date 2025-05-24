package com.jumuniyo.repository.store;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 사장님이 소유한 음식점 조회
     */
    List<Store> findByOwner(User owner);

    /**
     * 사장님이 소유한 음식점 조회 (페이징)
     */
    Page<Store> findByOwner(User owner, Pageable pageable);

    /**
     * 카테고리별 음식점 조회 (활성 상태만)
     */
    List<Store> findByCategoryAndIsActiveTrueAndIsApprovedTrue(Category category);

    /**
     * 카테고리별 음식점 조회 (페이징)
     */
    Page<Store> findByCategoryAndIsActiveTrueAndIsApprovedTrue(Category category, Pageable pageable);

    /**
     * 활성 상태이고 승인된 음식점만 조회
     */
    Page<Store> findByIsActiveTrueAndIsApprovedTrue(Pageable pageable);

    /**
     * 음식점 이름으로 검색 (활성 상태, 승인된 것만)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Store> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 사업자 등록번호로 조회
     */
    Optional<Store> findByBusinessNumber(String businessNumber);

    /**
     * 사업자 등록번호 중복 체크
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * 사업자 등록번호 중복 체크 (ID 제외, 수정 시 사용)
     */
    boolean existsByBusinessNumberAndIdNot(String businessNumber, Long id);

    /**
     * 승인 대기 중인 음식점 조회 (관리자용)
     */
    Page<Store> findByIsApprovedFalse(Pageable pageable);

    /**
     * 특정 지역의 음식점 조회
     */
    @Query("SELECT DISTINCT s FROM Store s JOIN s.deliveryAreas da " +
           "WHERE s.isActive = true AND s.isApproved = true " +
           "AND da.isActive = true " +
           "AND (LOWER(da.areaName) LIKE LOWER(CONCAT('%', :area, '%')) " +
           "OR LOWER(da.detailAddress) LIKE LOWER(CONCAT('%', :area, '%')))")
    Page<Store> findByDeliveryArea(@Param("area") String area, Pageable pageable);

    /**
     * 평점 순으로 음식점 조회
     */
    Page<Store> findByIsActiveTrueAndIsApprovedTrueOrderByRatingDesc(Pageable pageable);

    /**
     * 리뷰 수 순으로 음식점 조회
     */
    Page<Store> findByIsActiveTrueAndIsApprovedTrueOrderByReviewCountDesc(Pageable pageable);
} 