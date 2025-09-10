package com.jumuniyo.repository.store;

import com.jumuniyo.domain.store.OperatingHour;
import com.jumuniyo.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * 운영시간 엔티티를 위한 JPA Repository
 */
@Repository
public interface OperatingHourRepository extends JpaRepository<OperatingHour, Long> {

    /**
     * 특정 음식점의 모든 운영시간 조회
     */
    List<OperatingHour> findByStoreOrderByDayOfWeek(Store store);

    /**
     * 특정 음식점 ID의 모든 운영시간 조회
     */
    @Query("SELECT oh FROM OperatingHour oh WHERE oh.store.id = :storeId ORDER BY oh.dayOfWeek")
    List<OperatingHour> findByStoreIdOrderByDayOfWeek(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 특정 요일 운영시간 조회
     */
    Optional<OperatingHour> findByStoreAndDayOfWeek(Store store, DayOfWeek dayOfWeek);

    /**
     * 특정 음식점 ID의 특정 요일 운영시간 조회
     */
    @Query("SELECT oh FROM OperatingHour oh WHERE oh.store.id = :storeId AND oh.dayOfWeek = :dayOfWeek")
    Optional<OperatingHour> findByStoreIdAndDayOfWeek(@Param("storeId") Long storeId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    /**
     * 특정 음식점의 영업 중인 요일들만 조회
     */
    @Query("SELECT oh FROM OperatingHour oh WHERE oh.store.id = :storeId AND oh.isOpen = true ORDER BY oh.dayOfWeek")
    List<OperatingHour> findOpenDaysByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 운영시간 모두 삭제
     */
    void deleteByStore(Store store);

    /**
     * 특정 음식점 ID의 운영시간 모두 삭제
     */
    @Query("DELETE FROM OperatingHour oh WHERE oh.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);

    /**
     * 현재 영업 중인 음식점들의 운영시간 조회 (특정 요일)
     */
    @Query("SELECT oh FROM OperatingHour oh " +
           "WHERE oh.dayOfWeek = :dayOfWeek " +
           "AND oh.isOpen = true " +
           "AND oh.store.isActive = true " +
           "AND oh.store.isApproved = true")
    List<OperatingHour> findCurrentlyOpenStores(@Param("dayOfWeek") DayOfWeek dayOfWeek);
} 