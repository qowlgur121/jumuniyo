package com.jumuniyo.repository.store;

import com.jumuniyo.domain.store.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 활성 상태인 카테고리만 조회
     */
    List<Category> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * 카테고리 이름으로 조회
     */
    Optional<Category> findByName(String name);

    /**
     * 카테고리 이름으로 조회 (활성 상태만)
     */
    Optional<Category> findByNameAndIsActiveTrue(String name);

    /**
     * 표시 순서로 정렬된 모든 카테고리 조회
     */
    List<Category> findAllByOrderByDisplayOrder();

    /**
     * 특정 표시 순서보다 큰 카테고리들 조회
     */
    List<Category> findByDisplayOrderGreaterThanOrderByDisplayOrder(Integer displayOrder);

    /**
     * 카테고리명 중복 체크 (대소문자 무시)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(String name);

    /**
     * 카테고리명 중복 체크 (ID 제외, 수정 시 사용)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
} 