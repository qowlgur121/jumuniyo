package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.repository.store.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByDisplayOrder();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    @Override
    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));
    }

    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByNameAndIsActiveTrue(name)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + name));
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        log.info("Creating new category: {}", category.getName());
        
        // 카테고리명 중복 체크
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + category.getName());
        }
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        
        return savedCategory;
    }

    @Override
    @Transactional
    public Category updateCategory(Long categoryId, Category updateData) {
        Category existingCategory = getCategory(categoryId);
        
        // 카테고리명 중복 체크 (현재 카테고리 제외)
        if (!existingCategory.getName().equalsIgnoreCase(updateData.getName()) &&
            categoryRepository.existsByNameIgnoreCaseAndIdNot(updateData.getName(), categoryId)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + updateData.getName());
        }
        
        // 정보 업데이트
        existingCategory.updateName(updateData.getName());
        existingCategory.updateDescription(updateData.getDescription());
        existingCategory.updateIconUrl(updateData.getIconUrl());
        existingCategory.updateDisplayOrder(updateData.getDisplayOrder());
        
        log.info("Category updated successfully: {}", categoryId);
        return existingCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategory(categoryId);
        
        // 실제 삭제가 아닌 비활성화
        category.deactivate();
        log.info("Category deactivated: {}", categoryId);
    }

    @Override
    public boolean isCategoryNameDuplicate(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean isCategoryNameDuplicate(String name, Long excludeCategoryId) {
        return categoryRepository.existsByNameIgnoreCaseAndIdNot(name, excludeCategoryId);
    }
} 