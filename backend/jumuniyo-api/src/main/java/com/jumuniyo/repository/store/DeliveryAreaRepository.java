package com.jumuniyo.repository.store;

import com.jumuniyo.domain.store.DeliveryArea;
import com.jumuniyo.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 배달지역 엔티티를 위한 JPA Repository
 */
@Repository
public interface DeliveryAreaRepository extends JpaRepository<DeliveryArea, Long> {

    /**
     * 특정 음식점의 모든 배달지역 조회
     */
    List<DeliveryArea> findByStoreOrderByAreaName(Store store);

    /**
     * 특정 음식점 ID의 모든 배달지역 조회
     */
    @Query("SELECT da FROM DeliveryArea da WHERE da.store.id = :storeId ORDER BY da.areaName")
    List<DeliveryArea> findByStoreIdOrderByAreaName(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 활성화된 배달지역만 조회
     */
    @Query("SELECT da FROM DeliveryArea da WHERE da.store.id = :storeId AND da.isActive = true ORDER BY da.areaName")
    List<DeliveryArea> findActiveDeliveryAreasByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 특정 지역 이름으로 배달지역 조회
     */
    Optional<DeliveryArea> findByStoreAndAreaName(Store store, String areaName);

    /**
     * 특정 음식점 ID의 특정 지역 이름으로 배달지역 조회
     */
    @Query("SELECT da FROM DeliveryArea da WHERE da.store.id = :storeId AND da.areaName = :areaName")
    Optional<DeliveryArea> findByStoreIdAndAreaName(@Param("storeId") Long storeId, @Param("areaName") String areaName);

    /**
     * 특정 음식점의 배달지역 모두 삭제
     */
    void deleteByStore(Store store);

    /**
     * 특정 음식점 ID의 배달지역 모두 삭제
     */
    @Query("DELETE FROM DeliveryArea da WHERE da.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 주문 금액으로 배달 가능한 지역들 조회
     */
    @Query("SELECT da FROM DeliveryArea da " +
           "WHERE da.store.id = :storeId " +
           "AND da.isActive = true " +
           "AND da.minimumOrderAmount <= :orderAmount " +
           "ORDER BY da.areaName")
    List<DeliveryArea> findDeliveryAvailableAreas(@Param("storeId") Long storeId, @Param("orderAmount") BigDecimal orderAmount);

    /**
     * 특정 배달비 이하의 배달지역들 조회
     */
    @Query("SELECT da FROM DeliveryArea da " +
           "WHERE da.store.id = :storeId " +
           "AND da.isActive = true " +
           "AND da.deliveryFee <= :maxDeliveryFee " +
           "ORDER BY da.deliveryFee, da.areaName")
    List<DeliveryArea> findByMaxDeliveryFee(@Param("storeId") Long storeId, @Param("maxDeliveryFee") BigDecimal maxDeliveryFee);

    /**
     * 배달 가능한 모든 음식점의 지역들 조회 (특정 지역명으로)
     */
    @Query("SELECT da FROM DeliveryArea da " +
           "WHERE da.areaName LIKE %:areaName% " +
           "AND da.isActive = true " +
           "AND da.store.isActive = true " +
           "AND da.store.isApproved = true " +
           "ORDER BY da.deliveryFee")
    List<DeliveryArea> findAvailableStoresByAreaName(@Param("areaName") String areaName);
} 