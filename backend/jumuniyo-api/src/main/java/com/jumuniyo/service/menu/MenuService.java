package com.jumuniyo.service.menu;

import com.jumuniyo.dto.menu.MenuRequestDto;
import com.jumuniyo.dto.menu.MenuResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MenuService {

    /**
     * 메뉴 생성
     */
    MenuResponseDto createMenu(Long storeId, MenuRequestDto requestDto, Long ownerId);

    /**
     * 메뉴 생성 (이미지 포함)
     */
    MenuResponseDto createMenuWithImage(Long storeId, MenuRequestDto requestDto, MultipartFile imageFile, Long ownerId);

    /**
     * 메뉴 수정
     */
    MenuResponseDto updateMenu(Long storeId, Long menuId, MenuRequestDto requestDto, Long ownerId);

    /**
     * 메뉴 이미지 수정
     */
    MenuResponseDto updateMenuImage(Long storeId, Long menuId, MultipartFile imageFile, Long ownerId);

    /**
     * 메뉴 삭제
     */
    void deleteMenu(Long storeId, Long menuId, Long ownerId);

    /**
     * 특정 음식점의 모든 메뉴 조회
     */
    List<MenuResponseDto> getMenusByStore(Long storeId);

    /**
     * 특정 음식점의 판매 가능한 메뉴만 조회
     */
    List<MenuResponseDto> getAvailableMenusByStore(Long storeId);

    /**
     * 특정 카테고리의 메뉴 조회
     */
    List<MenuResponseDto> getMenusByCategory(Long categoryId);

    /**
     * 특정 카테고리의 판매 가능한 메뉴만 조회
     */
    List<MenuResponseDto> getAvailableMenusByCategory(Long categoryId);

    /**
     * 메뉴 단건 조회
     */
    MenuResponseDto getMenu(Long storeId, Long menuId);

    /**
     * 메뉴 판매 가능 여부 토글
     */
    MenuResponseDto toggleMenuAvailability(Long storeId, Long menuId, Long ownerId);

    /**
     * 메뉴 추천 여부 토글
     */
    MenuResponseDto toggleMenuRecommendation(Long storeId, Long menuId, Long ownerId);

    /**
     * 메뉴 표시 순서 변경
     */
    List<MenuResponseDto> updateDisplayOrder(Long storeId, Long categoryId, List<Long> menuIds, Long ownerId);

    /**
     * 추천 메뉴 조회
     */
    List<MenuResponseDto> getRecommendedMenus(Long storeId);

    /**
     * 인기 메뉴 조회 (판매량 기준)
     */
    List<MenuResponseDto> getPopularMenus(Long storeId);
} 