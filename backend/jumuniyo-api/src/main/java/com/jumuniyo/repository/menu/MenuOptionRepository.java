package com.jumuniyo.repository.menu;

import com.jumuniyo.domain.menu.MenuOption;
import com.jumuniyo.domain.menu.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {

    /**
     * 특정 옵션 그룹의 모든 옵션 조회 (표시 순서대로)
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.optionGroup.id = :optionGroupId ORDER BY mo.displayOrder ASC, mo.id ASC")
    List<MenuOption> findByOptionGroupIdOrderByDisplayOrder(@Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 옵션 그룹의 사용 가능한 옵션만 조회
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.optionGroup.id = :optionGroupId AND mo.isAvailable = true ORDER BY mo.displayOrder ASC, mo.id ASC")
    List<MenuOption> findAvailableByOptionGroupIdOrderByDisplayOrder(@Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 옵션 그룹의 기본 옵션 조회
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.optionGroup.id = :optionGroupId AND mo.isDefault = true")
    List<MenuOption> findDefaultByOptionGroupId(@Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 옵션 그룹의 옵션 개수 조회
     */
    long countByOptionGroup(MenuOptionGroup optionGroup);

    /**
     * 특정 옵션 그룹의 사용 가능한 옵션 개수 조회
     */
    @Query("SELECT COUNT(mo) FROM MenuOption mo WHERE mo.optionGroup.id = :optionGroupId AND mo.isAvailable = true")
    long countAvailableByOptionGroupId(@Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 옵션 그룹에서 옵션 이름으로 조회
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.name = :name AND mo.optionGroup.id = :optionGroupId")
    Optional<MenuOption> findByNameAndOptionGroupId(@Param("name") String name, @Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 옵션 그룹에서 옵션 이름 중복 체크 (자신 제외)
     */
    @Query("SELECT COUNT(mo) > 0 FROM MenuOption mo WHERE mo.name = :name AND mo.optionGroup.id = :optionGroupId AND mo.id != :excludeId")
    boolean existsByNameAndOptionGroupIdAndIdNot(@Param("name") String name, @Param("optionGroupId") Long optionGroupId, @Param("excludeId") Long excludeId);

    /**
     * 특정 옵션 그룹에서 옵션 이름 중복 체크
     */
    @Query("SELECT COUNT(mo) > 0 FROM MenuOption mo WHERE mo.name = :name AND mo.optionGroup.id = :optionGroupId")
    boolean existsByNameAndOptionGroupId(@Param("name") String name, @Param("optionGroupId") Long optionGroupId);

    /**
     * 특정 메뉴의 모든 옵션 조회 (옵션 그룹과 옵션 표시 순서대로)
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.optionGroup.menu.id = :menuId ORDER BY mo.optionGroup.displayOrder ASC, mo.displayOrder ASC")
    List<MenuOption> findByMenuIdOrderByDisplayOrder(@Param("menuId") Long menuId);

    /**
     * 특정 메뉴의 사용 가능한 옵션만 조회
     */
    @Query("SELECT mo FROM MenuOption mo WHERE mo.optionGroup.menu.id = :menuId AND mo.isAvailable = true AND mo.optionGroup.isActive = true ORDER BY mo.optionGroup.displayOrder ASC, mo.displayOrder ASC")
    List<MenuOption> findAvailableByMenuIdOrderByDisplayOrder(@Param("menuId") Long menuId);
} 