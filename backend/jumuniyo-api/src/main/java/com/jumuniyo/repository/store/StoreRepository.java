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

import java.math.BigDecimal;
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
     * 사장님이 소유한 활성화된 음식점 조회 (페이징)
     */
    Page<Store> findByOwnerAndIsActiveTrue(User owner, Pageable pageable);

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

    /**
     * 위치 정보가 있는 활성화되고 승인된 음식점 조회 (거리 계산을 위해)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL")
    List<Store> findActiveStoresWithLocation();

    /**
     * 키워드 검색 + 위치 필터링 (위치 정보가 있는 음식점만)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Store> searchByKeywordWithLocation(@Param("keyword") String keyword);

    /**
     * 카테고리별 음식점 조회 (위치 정보가 있는 것만)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "AND s.category.id = :categoryId")
    List<Store> findByCategoryWithLocation(@Param("categoryId") Long categoryId);

    /**
     * 카테고리별 + 키워드 검색 (위치 정보가 있는 것만)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "AND s.category.id = :categoryId " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Store> findByCategoryAndKeywordWithLocation(@Param("categoryId") Long categoryId, 
                                                     @Param("keyword") String keyword);

    /**
     * 지역별 + 위치 정보가 있는 음식점 조회
     */
    @Query("SELECT DISTINCT s FROM Store s JOIN s.deliveryAreas da " +
           "WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL " +
           "AND da.isActive = true " +
           "AND (LOWER(da.areaName) LIKE LOWER(CONCAT('%', :area, '%')) " +
           "OR LOWER(da.detailAddress) LIKE LOWER(CONCAT('%', :area, '%')))")
    List<Store> findByDeliveryAreaWithLocation(@Param("area") String area);

    /**
     * 필터링 조건을 적용한 음식점 검색 (키워드 + 필터)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxMinimumOrderAmount IS NULL OR s.minimumOrderAmount <= :maxMinimumOrderAmount) " +
           "AND (:maxDeliveryFee IS NULL OR s.deliveryFee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.reviewCount >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.deliveryFee = 0)")
    Page<Store> searchByKeywordWithFilters(@Param("keyword") String keyword,
                                          @Param("minRating") BigDecimal minRating,
                                          @Param("maxMinimumOrderAmount") BigDecimal maxMinimumOrderAmount,
                                          @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
                                          @Param("minReviewCount") Integer minReviewCount,
                                          @Param("freeDeliveryOnly") Boolean freeDeliveryOnly,
                                          Pageable pageable);

    /**
     * 필터링 조건을 적용한 음식점 검색 (카테고리 + 필터)
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND s.category.id = :categoryId " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxMinimumOrderAmount IS NULL OR s.minimumOrderAmount <= :maxMinimumOrderAmount) " +
           "AND (:maxDeliveryFee IS NULL OR s.deliveryFee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.reviewCount >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.deliveryFee = 0)")
    Page<Store> findByCategoryWithFilters(@Param("categoryId") Long categoryId,
                                         @Param("minRating") BigDecimal minRating,
                                         @Param("maxMinimumOrderAmount") BigDecimal maxMinimumOrderAmount,
                                         @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
                                         @Param("minReviewCount") Integer minReviewCount,
                                         @Param("freeDeliveryOnly") Boolean freeDeliveryOnly,
                                         Pageable pageable);

    /**
     * 필터링 조건을 적용한 전체 음식점 조회
     */
    @Query("SELECT s FROM Store s WHERE s.isActive = true AND s.isApproved = true " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxMinimumOrderAmount IS NULL OR s.minimumOrderAmount <= :maxMinimumOrderAmount) " +
           "AND (:maxDeliveryFee IS NULL OR s.deliveryFee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.reviewCount >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.deliveryFee = 0)")
    Page<Store> findAllWithFilters(@Param("minRating") BigDecimal minRating,
                                  @Param("maxMinimumOrderAmount") BigDecimal maxMinimumOrderAmount,
                                  @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
                                  @Param("minReviewCount") Integer minReviewCount,
                                  @Param("freeDeliveryOnly") Boolean freeDeliveryOnly,
                                  Pageable pageable);

    /**
     * 필터링 조건을 적용한 지역별 음식점 조회
     */
    @Query("SELECT DISTINCT s FROM Store s JOIN s.deliveryAreas da " +
           "WHERE s.isActive = true AND s.isApproved = true " +
           "AND da.isActive = true " +
           "AND (LOWER(da.areaName) LIKE LOWER(CONCAT('%', :area, '%')) " +
           "OR LOWER(da.detailAddress) LIKE LOWER(CONCAT('%', :area, '%'))) " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxMinimumOrderAmount IS NULL OR s.minimumOrderAmount <= :maxMinimumOrderAmount) " +
           "AND (:maxDeliveryFee IS NULL OR s.deliveryFee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.reviewCount >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.deliveryFee = 0) " +
           "AND (:maxDeliveryTime IS NULL OR da.deliveryTimeMinutes <= :maxDeliveryTime)")
    Page<Store> findByDeliveryAreaWithFilters(@Param("area") String area,
                                             @Param("minRating") BigDecimal minRating,
                                             @Param("maxMinimumOrderAmount") BigDecimal maxMinimumOrderAmount,
                                             @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
                                             @Param("maxDeliveryTime") Integer maxDeliveryTime,
                                             @Param("minReviewCount") Integer minReviewCount,
                                             @Param("freeDeliveryOnly") Boolean freeDeliveryOnly,
                                             Pageable pageable);

    /**
     * 공간 인덱스를 활용한 반경 내 음식점 검색
     * MySQL 8.0 이상의 ST_Distance_Sphere 함수 사용
     */
    @Query(value = "SELECT s.* FROM stores s " +
           "WHERE s.is_active = true AND s.is_approved = true " +
           "AND ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") <= :radiusMeters " +
           "ORDER BY ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") ASC", 
           nativeQuery = true)
    List<Store> findStoresWithinRadius(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters);

    /**
     * 공간 인덱스를 활용한 반경 내 음식점 검색 (페이징)
     */
    @Query(value = "SELECT s.* FROM stores s " +
           "WHERE s.is_active = true AND s.is_approved = true " +
           "AND ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") <= :radiusMeters " +
           "ORDER BY ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") ASC " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Store> findStoresWithinRadiusPaged(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 공간 인덱스를 활용한 반경 내 음식점 개수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM stores s " +
           "WHERE s.is_active = true AND s.is_approved = true " +
           "AND ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") <= :radiusMeters", 
           nativeQuery = true)
    long countStoresWithinRadius(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters);

    /**
     * 공간 인덱스를 활용한 반경 내 음식점 검색 (키워드 + 필터링)
     */
    @Query(value = "SELECT s.* FROM stores s " +
           "WHERE s.is_active = true AND s.is_approved = true " +
           "AND ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") <= :radiusMeters " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxDeliveryFee IS NULL OR s.delivery_fee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.review_count >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.delivery_fee = 0) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'distance' THEN ST_Distance_Sphere(s.location, POINT(:longitude, :latitude)) END ASC, " +
           "CASE WHEN :sortBy = 'rating' THEN s.rating END DESC, " +
           "CASE WHEN :sortBy = 'reviewCount' THEN s.review_count END DESC, " +
           "CASE WHEN :sortBy = 'newest' THEN s.created_at END DESC, " +
           "CASE WHEN :sortBy = 'deliveryFee' THEN s.delivery_fee END ASC " +
           "LIMIT :limit OFFSET :offset", 
           nativeQuery = true)
    List<Store> findStoresWithinRadiusWithFilters(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("keyword") String keyword,
            @Param("minRating") BigDecimal minRating,
            @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
            @Param("minReviewCount") Integer minReviewCount,
            @Param("freeDeliveryOnly") Boolean freeDeliveryOnly,
            @Param("sortBy") String sortBy,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 공간 인덱스를 활용한 반경 내 음식점 개수 조회 (키워드 + 필터링)
     */
    @Query(value = "SELECT COUNT(*) FROM stores s " +
           "WHERE s.is_active = true AND s.is_approved = true " +
           "AND ST_Distance_Sphere(" +
           "    s.location, " +
           "    POINT(:longitude, :latitude)" +
           ") <= :radiusMeters " +
           "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:minRating IS NULL OR s.rating >= :minRating) " +
           "AND (:maxDeliveryFee IS NULL OR s.delivery_fee <= :maxDeliveryFee) " +
           "AND (:minReviewCount IS NULL OR s.review_count >= :minReviewCount) " +
           "AND (:freeDeliveryOnly = false OR s.delivery_fee = 0)", 
           nativeQuery = true)
    long countStoresWithinRadiusWithFilters(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("keyword") String keyword,
            @Param("minRating") BigDecimal minRating,
            @Param("maxDeliveryFee") BigDecimal maxDeliveryFee,
            @Param("minReviewCount") Integer minReviewCount,
            @Param("freeDeliveryOnly") Boolean freeDeliveryOnly);
} 