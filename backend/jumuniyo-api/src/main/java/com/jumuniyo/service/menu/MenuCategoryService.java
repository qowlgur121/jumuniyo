package com.jumuniyo.service.menu;

import com.jumuniyo.dto.menu.MenuCategoryRequestDto;
import com.jumuniyo.dto.menu.MenuCategoryResponseDto;

import java.util.List;

public interface MenuCategoryService {

    /**
     * 메뉴 카테고리 생성
     */
    MenuCategoryResponseDto createMenuCategory(Long storeId, MenuCategoryRequestDto requestDto, Long ownerId);

    /**
     * 메뉴 카테고리 수정
     */
    MenuCategoryResponseDto updateMenuCategory(Long storeId, Long categoryId, MenuCategoryRequestDto requestDto, Long ownerId);

    /**
     * 메뉴 카테고리 삭제
     */
    void deleteMenuCategory(Long storeId, Long categoryId, Long ownerId);

    /**
     * 특정 음식점의 모든 메뉴 카테고리 조회
     */
    List<MenuCategoryResponseDto> getMenuCategoriesByStore(Long storeId);

    /**
     * 특정 음식점의 활성화된 메뉴 카테고리만 조회
     */
    List<MenuCategoryResponseDto> getActiveMenuCategoriesByStore(Long storeId);

    /**
     * 메뉴 카테고리 단건 조회
     */
    MenuCategoryResponseDto getMenuCategory(Long storeId, Long categoryId);

    /**
     * 메뉴 카테고리 활성화/비활성화
     */
    MenuCategoryResponseDto toggleMenuCategoryStatus(Long storeId, Long categoryId, Long ownerId);

    /**
     * 메뉴 카테고리 표시 순서 변경
     */
    List<MenuCategoryResponseDto> updateDisplayOrder(Long storeId, List<Long> categoryIds, Long ownerId);
} 