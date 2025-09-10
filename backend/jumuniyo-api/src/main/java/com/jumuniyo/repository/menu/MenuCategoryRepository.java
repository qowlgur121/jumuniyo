package com.jumuniyo.repository.menu;

import com.jumuniyo.domain.menu.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /**
     * 특정 음식점의 모든 메뉴 카테고리 조회 (표시 순서대로 정렬)
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.store.id = :storeId ORDER BY mc.displayOrder ASC, mc.id ASC")
    List<MenuCategory> findByStoreIdOrderByDisplayOrder(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 활성화된 메뉴 카테고리만 조회 (표시 순서대로 정렬)
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.store.id = :storeId AND mc.isActive = true ORDER BY mc.displayOrder ASC, mc.id ASC")
    List<MenuCategory> findActiveByStoreIdOrderByDisplayOrder(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 특정 카테고리 조회
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.id = :categoryId AND mc.store.id = :storeId")
    Optional<MenuCategory> findByIdAndStoreId(@Param("categoryId") Long categoryId, @Param("storeId") Long storeId);

    /**
     * 특정 음식점에서 카테고리 이름으로 조회
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.name = :name AND mc.store.id = :storeId")
    Optional<MenuCategory> findByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);

    /**
     * 특정 음식점의 카테고리 개수 조회
     */
    @Query("SELECT COUNT(mc) FROM MenuCategory mc WHERE mc.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 활성화된 카테고리 개수 조회
     */
    @Query("SELECT COUNT(mc) FROM MenuCategory mc WHERE mc.store.id = :storeId AND mc.isActive = true")
    long countActiveByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 음식점에서 특정 표시 순서보다 큰 카테고리들 조회
     */
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.store.id = :storeId AND mc.displayOrder > :displayOrder ORDER BY mc.displayOrder ASC")
    List<MenuCategory> findByStoreIdAndDisplayOrderGreaterThan(@Param("storeId") Long storeId, @Param("displayOrder") Integer displayOrder);

    /**
     * 특정 음식점에서 카테고리 이름 중복 체크 (자신 제외)
     */
    @Query("SELECT COUNT(mc) > 0 FROM MenuCategory mc WHERE mc.name = :name AND mc.store.id = :storeId AND mc.id != :excludeId")
    boolean existsByNameAndStoreIdAndIdNot(@Param("name") String name, @Param("storeId") Long storeId, @Param("excludeId") Long excludeId);

    /**
     * 특정 음식점에서 카테고리 이름 중복 체크
     */
    @Query("SELECT COUNT(mc) > 0 FROM MenuCategory mc WHERE mc.name = :name AND mc.store.id = :storeId")
    boolean existsByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);
} 