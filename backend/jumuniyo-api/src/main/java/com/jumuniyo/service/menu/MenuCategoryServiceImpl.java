package com.jumuniyo.service.menu;

import com.jumuniyo.domain.menu.MenuCategory;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.dto.menu.MenuCategoryRequestDto;
import com.jumuniyo.dto.menu.MenuCategoryResponseDto;
import com.jumuniyo.exception.BusinessException;
import com.jumuniyo.exception.ErrorCode;
import com.jumuniyo.repository.menu.MenuCategoryRepository;
import com.jumuniyo.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuCategoryServiceImpl implements MenuCategoryService {

    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public MenuCategoryResponseDto createMenuCategory(Long storeId, MenuCategoryRequestDto requestDto, Long ownerId) {
        log.info("Creating menu category for store: {}, owner: {}", storeId, ownerId);

        // 음식점 조회 및 권한 확인
        Store store = getStoreAndValidateOwnership(storeId, ownerId);

        // 카테고리 이름 중복 체크
        if (menuCategoryRepository.existsByNameAndStoreId(requestDto.getName(), storeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }

        // 표시 순서 설정 (미지정시 마지막 순서로)
        Integer displayOrder = requestDto.getDisplayOrder();
        if (displayOrder == null) {
            long categoryCount = menuCategoryRepository.countByStoreId(storeId);
            displayOrder = (int) categoryCount;
        }

        // 메뉴 카테고리 생성
        MenuCategory menuCategory = MenuCategory.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .displayOrder(displayOrder)
                .store(store)
                .build();

        MenuCategory savedCategory = menuCategoryRepository.save(menuCategory);
        log.info("Menu category created successfully: {}", savedCategory.getId());

        return MenuCategoryResponseDto.from(savedCategory);
    }

    @Override
    @Transactional
    public MenuCategoryResponseDto updateMenuCategory(Long storeId, Long categoryId, MenuCategoryRequestDto requestDto, Long ownerId) {
        log.info("Updating menu category: {}, store: {}, owner: {}", categoryId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 카테고리 조회
        MenuCategory menuCategory = getMenuCategoryByStoreAndId(storeId, categoryId);

        // 카테고리 이름 중복 체크 (자신 제외)
        if (menuCategoryRepository.existsByNameAndStoreIdAndIdNot(requestDto.getName(), storeId, categoryId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_CATEGORY_NAME);
        }

        // 정보 업데이트
        menuCategory.updateInfo(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getDisplayOrder() != null ? requestDto.getDisplayOrder() : menuCategory.getDisplayOrder()
        );

        log.info("Menu category updated successfully: {}", categoryId);
        return MenuCategoryResponseDto.from(menuCategory);
    }

    @Override
    @Transactional
    public void deleteMenuCategory(Long storeId, Long categoryId, Long ownerId) {
        log.info("Deleting menu category: {}, store: {}, owner: {}", categoryId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 카테고리 조회
        MenuCategory menuCategory = getMenuCategoryByStoreAndId(storeId, categoryId);

        // 해당 카테고리에 메뉴가 있는지 확인
        if (!menuCategory.getMenus().isEmpty()) {
            throw new BusinessException(ErrorCode.MENU_CATEGORY_HAS_MENUS);
        }

        menuCategoryRepository.delete(menuCategory);
        log.info("Menu category deleted successfully: {}", categoryId);
    }

    @Override
    public List<MenuCategoryResponseDto> getMenuCategoriesByStore(Long storeId) {
        log.info("Getting all menu categories for store: {}", storeId);

        List<MenuCategory> categories = menuCategoryRepository.findByStoreIdOrderByDisplayOrder(storeId);
        return categories.stream()
                .map(MenuCategoryResponseDto::from)
                .toList();
    }

    @Override
    public List<MenuCategoryResponseDto> getActiveMenuCategoriesByStore(Long storeId) {
        log.info("Getting active menu categories for store: {}", storeId);

        List<MenuCategory> categories = menuCategoryRepository.findActiveByStoreIdOrderByDisplayOrder(storeId);
        return categories.stream()
                .map(MenuCategoryResponseDto::from)
                .toList();
    }

    @Override
    public MenuCategoryResponseDto getMenuCategory(Long storeId, Long categoryId) {
        log.info("Getting menu category: {}, store: {}", categoryId, storeId);

        MenuCategory menuCategory = getMenuCategoryByStoreAndId(storeId, categoryId);
        return MenuCategoryResponseDto.from(menuCategory);
    }

    @Override
    @Transactional
    public MenuCategoryResponseDto toggleMenuCategoryStatus(Long storeId, Long categoryId, Long ownerId) {
        log.info("Toggling menu category status: {}, store: {}, owner: {}", categoryId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 카테고리 조회
        MenuCategory menuCategory = getMenuCategoryByStoreAndId(storeId, categoryId);

        // 상태 토글
        if (menuCategory.getIsActive()) {
            menuCategory.deactivate();
        } else {
            menuCategory.activate();
        }

        log.info("Menu category status toggled: {}, new status: {}", categoryId, menuCategory.getIsActive());
        return MenuCategoryResponseDto.from(menuCategory);
    }

    @Override
    @Transactional
    public List<MenuCategoryResponseDto> updateDisplayOrder(Long storeId, List<Long> categoryIds, Long ownerId) {
        log.info("Updating display order for store: {}, owner: {}", storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 카테고리들 조회
        List<MenuCategory> categories = categoryIds.stream()
                .map(categoryId -> getMenuCategoryByStoreAndId(storeId, categoryId))
                .toList();

        // 표시 순서 업데이트
        IntStream.range(0, categories.size())
                .forEach(i -> categories.get(i).updateDisplayOrder(i));

        log.info("Display order updated for {} categories", categories.size());

        return categories.stream()
                .map(MenuCategoryResponseDto::from)
                .toList();
    }

    // 헬퍼 메소드들
    private Store getStoreAndValidateOwnership(Long storeId, Long ownerId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (!store.isOwnedBy(ownerId)) {
            throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
        }

        return store;
    }

    private MenuCategory getMenuCategoryByStoreAndId(Long storeId, Long categoryId) {
        return menuCategoryRepository.findByIdAndStoreId(categoryId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    }
} 