package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 모든 카테고리 조회 (표시 순서대로)
     */
    List<Category> getAllCategories();

    /**
     * 활성 카테고리만 조회 (표시 순서대로)
     */
    List<Category> getActiveCategories();

    /**
     * 카테고리 조회
     */
    Category getCategory(Long categoryId);

    /**
     * 카테고리 이름으로 조회
     */
    Category getCategoryByName(String name);

    /**
     * 카테고리 생성 (관리자용)
     */
    Category createCategory(Category category);

    /**
     * 카테고리 수정 (관리자용)
     */
    Category updateCategory(Long categoryId, Category updateData);

    /**
     * 카테고리 삭제 (관리자용)
     */
    void deleteCategory(Long categoryId);

    /**
     * 카테고리명 중복 체크
     */
    boolean isCategoryNameDuplicate(String name);

    /**
     * 카테고리명 중복 체크 (수정 시)
     */
    boolean isCategoryNameDuplicate(String name, Long excludeCategoryId);
} 