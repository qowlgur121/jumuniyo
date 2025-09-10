package com.jumuniyo.repository.menu;

import com.jumuniyo.domain.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * 특정 음식점의 모든 메뉴 조회 (표시 순서대로 정렬)
     */
    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId ORDER BY m.category.displayOrder ASC, m.displayOrder ASC, m.id ASC")
    List<Menu> findByStoreIdOrderByDisplayOrder(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 판매 가능한 메뉴만 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND m.isAvailable = true ORDER BY m.category.displayOrder ASC, m.displayOrder ASC, m.id ASC")
    List<Menu> findAvailableByStoreIdOrderByDisplayOrder(@Param("storeId") Long storeId);

    /**
     * 특정 카테고리의 모든 메뉴 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.category.id = :categoryId ORDER BY m.displayOrder ASC, m.id ASC")
    List<Menu> findByCategoryIdOrderByDisplayOrder(@Param("categoryId") Long categoryId);

    /**
     * 특정 카테고리의 판매 가능한 메뉴만 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.category.id = :categoryId AND m.isAvailable = true ORDER BY m.displayOrder ASC, m.id ASC")
    List<Menu> findAvailableByCategoryIdOrderByDisplayOrder(@Param("categoryId") Long categoryId);

    /**
     * 특정 음식점의 특정 메뉴 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.id = :menuId AND m.store.id = :storeId")
    Optional<Menu> findByIdAndStoreId(@Param("menuId") Long menuId, @Param("storeId") Long storeId);

    /**
     * 특정 음식점에서 메뉴 이름으로 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.name = :name AND m.store.id = :storeId")
    Optional<Menu> findByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);

    /**
     * 특정 음식점의 메뉴 개수 조회
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 음식점의 판매 가능한 메뉴 개수 조회
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.store.id = :storeId AND m.isAvailable = true")
    long countAvailableByStoreId(@Param("storeId") Long storeId);

    /**
     * 특정 카테고리의 메뉴 개수 조회
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.category.id = :categoryId")
    long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 특정 음식점에서 메뉴 이름 중복 체크 (자신 제외)
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.name = :name AND m.store.id = :storeId AND m.id != :excludeId")
    boolean existsByNameAndStoreIdAndIdNot(@Param("name") String name, @Param("storeId") Long storeId, @Param("excludeId") Long excludeId);

    /**
     * 특정 음식점에서 메뉴 이름 중복 체크
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m WHERE m.name = :name AND m.store.id = :storeId")
    boolean existsByNameAndStoreId(@Param("name") String name, @Param("storeId") Long storeId);

    /**
     * 추천 메뉴 조회
     */
    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND m.isRecommended = true AND m.isAvailable = true ORDER BY m.displayOrder ASC")
    List<Menu> findRecommendedByStoreId(@Param("storeId") Long storeId);

    /**
     * 인기 메뉴 조회 (판매량 기준)
     */
    @Query("SELECT m FROM Menu m WHERE m.store.id = :storeId AND m.isAvailable = true ORDER BY m.soldCount DESC, m.displayOrder ASC")
    List<Menu> findPopularByStoreId(@Param("storeId") Long storeId);
} 