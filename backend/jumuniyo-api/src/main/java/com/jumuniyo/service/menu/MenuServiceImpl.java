package com.jumuniyo.service.menu;

import com.jumuniyo.domain.menu.Menu;
import com.jumuniyo.domain.menu.MenuCategory;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.dto.menu.MenuRequestDto;
import com.jumuniyo.dto.menu.MenuResponseDto;
import com.jumuniyo.exception.BusinessException;
import com.jumuniyo.exception.ErrorCode;
import com.jumuniyo.repository.menu.MenuCategoryRepository;
import com.jumuniyo.repository.menu.MenuRepository;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.service.file.FileUploadService;
import com.jumuniyo.service.price.PriceCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreRepository storeRepository;
    private final FileUploadService fileUploadService;
    private final PriceCalculationService priceCalculationService;

    @Override
    @Transactional
    public MenuResponseDto createMenu(Long storeId, MenuRequestDto requestDto, Long ownerId) {
        log.info("Creating menu for store: {}, owner: {}", storeId, ownerId);

        // 음식점 조회 및 권한 확인
        Store store = getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 카테고리 조회
        MenuCategory category = getMenuCategoryByStoreAndId(storeId, requestDto.getCategoryId());

        // 메뉴 이름 중복 체크
        if (menuRepository.existsByNameAndStoreId(requestDto.getName(), storeId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_NAME);
        }

        // 표시 순서 설정 (미지정시 해당 카테고리의 마지막 순서로)
        Integer displayOrder = requestDto.getDisplayOrder();
        if (displayOrder == null) {
            long menuCount = menuRepository.countByCategoryId(requestDto.getCategoryId());
            displayOrder = (int) menuCount;
        }

        // 메뉴 생성
        Menu menu = Menu.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .imageUrl(requestDto.getImageUrl())
                .isRecommended(requestDto.getIsRecommended())
                .displayOrder(displayOrder)
                .store(store)
                .category(category)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        log.info("Menu created successfully: {}", savedMenu.getId());

        return MenuResponseDto.fromWithFormattedPrice(savedMenu, 
                priceCalculationService.formatPrice(savedMenu.getPrice()));
    }

    @Override
    @Transactional
    public MenuResponseDto createMenuWithImage(Long storeId, MenuRequestDto requestDto, MultipartFile imageFile, Long ownerId) {
        log.info("Creating menu with image for store: {}, owner: {}", storeId, ownerId);

        // 이미지 업로드 (임시 메뉴 ID로 업로드 후 실제 ID로 변경 필요)
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            // 임시로 현재 시간을 메뉴 ID로 사용
            Long tempMenuId = System.currentTimeMillis();
            imageUrl = fileUploadService.uploadMenuImage(imageFile, storeId, tempMenuId);
        }

        // 이미지 URL을 포함한 요청 DTO 생성
        MenuRequestDto requestWithImage = MenuRequestDto.withImage(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getPrice(),
                imageUrl,
                requestDto.getCategoryId(),
                requestDto.getIsRecommended(),
                requestDto.getDisplayOrder()
        );

        return createMenu(storeId, requestWithImage, ownerId);
    }

    @Override
    @Transactional
    public MenuResponseDto updateMenu(Long storeId, Long menuId, MenuRequestDto requestDto, Long ownerId) {
        log.info("Updating menu: {}, store: {}, owner: {}", menuId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 조회
        Menu menu = getMenuByStoreAndId(storeId, menuId);

        // 메뉴 카테고리 조회 (카테고리 변경시)
        MenuCategory category = null;
        if (!menu.getCategory().getId().equals(requestDto.getCategoryId())) {
            category = getMenuCategoryByStoreAndId(storeId, requestDto.getCategoryId());
        }

        // 메뉴 이름 중복 체크 (자신 제외)
        if (menuRepository.existsByNameAndStoreIdAndIdNot(requestDto.getName(), storeId, menuId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_MENU_NAME);
        }

        // 정보 업데이트
        menu.updateBasicInfo(
                requestDto.getName(),
                requestDto.getDescription(),
                requestDto.getPrice(),
                requestDto.getImageUrl() != null ? requestDto.getImageUrl() : menu.getImageUrl()
        );

        if (category != null) {
            menu.updateCategory(category);
        }

        if (requestDto.getIsRecommended() != null) {
            menu.setRecommended(requestDto.getIsRecommended());
        }

        if (requestDto.getDisplayOrder() != null) {
            menu.updateDisplayOrder(requestDto.getDisplayOrder());
        }

        log.info("Menu updated successfully: {}", menuId);
        return MenuResponseDto.fromWithFormattedPrice(menu, 
                priceCalculationService.formatPrice(menu.getPrice()));
    }

    @Override
    @Transactional
    public MenuResponseDto updateMenuImage(Long storeId, Long menuId, MultipartFile imageFile, Long ownerId) {
        log.info("Updating menu image: {}, store: {}, owner: {}", menuId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 조회
        Menu menu = getMenuByStoreAndId(storeId, menuId);

        // 기존 이미지 삭제
        if (menu.getImageUrl() != null) {
            fileUploadService.deleteFile(menu.getImageUrl());
        }

        // 새 이미지 업로드
        String newImageUrl = fileUploadService.uploadMenuImage(imageFile, storeId, menuId);
        menu.updateBasicInfo(menu.getName(), menu.getDescription(), menu.getPrice(), newImageUrl);

        log.info("Menu image updated successfully: {}", menuId);
        return MenuResponseDto.fromWithFormattedPrice(menu, 
                priceCalculationService.formatPrice(menu.getPrice()));
    }

    @Override
    @Transactional
    public void deleteMenu(Long storeId, Long menuId, Long ownerId) {
        log.info("Deleting menu: {}, store: {}, owner: {}", menuId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 조회
        Menu menu = getMenuByStoreAndId(storeId, menuId);

        // 이미지 파일 삭제
        if (menu.getImageUrl() != null) {
            fileUploadService.deleteFile(menu.getImageUrl());
        }

        menuRepository.delete(menu);
        log.info("Menu deleted successfully: {}", menuId);
    }

    @Override
    public List<MenuResponseDto> getMenusByStore(Long storeId) {
        log.info("Getting all menus for store: {}", storeId);

        List<Menu> menus = menuRepository.findByStoreIdOrderByDisplayOrder(storeId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public List<MenuResponseDto> getAvailableMenusByStore(Long storeId) {
        log.info("Getting available menus for store: {}", storeId);

        List<Menu> menus = menuRepository.findAvailableByStoreIdOrderByDisplayOrder(storeId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public List<MenuResponseDto> getMenusByCategory(Long categoryId) {
        log.info("Getting menus for category: {}", categoryId);

        List<Menu> menus = menuRepository.findByCategoryIdOrderByDisplayOrder(categoryId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public List<MenuResponseDto> getAvailableMenusByCategory(Long categoryId) {
        log.info("Getting available menus for category: {}", categoryId);

        List<Menu> menus = menuRepository.findAvailableByCategoryIdOrderByDisplayOrder(categoryId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public MenuResponseDto getMenu(Long storeId, Long menuId) {
        log.info("Getting menu: {}, store: {}", menuId, storeId);

        Menu menu = getMenuByStoreAndId(storeId, menuId);
        return MenuResponseDto.fromWithFormattedPrice(menu, 
                priceCalculationService.formatPrice(menu.getPrice()));
    }

    @Override
    @Transactional
    public MenuResponseDto toggleMenuAvailability(Long storeId, Long menuId, Long ownerId) {
        log.info("Toggling menu availability: {}, store: {}, owner: {}", menuId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 조회
        Menu menu = getMenuByStoreAndId(storeId, menuId);

        // 판매 가능 여부 토글
        if (menu.getIsAvailable()) {
            menu.makeUnavailable();
            log.info("Menu {} set to unavailable", menuId);
        } else {
            menu.makeAvailable();
            log.info("Menu {} set to available", menuId);
        }

        log.info("Menu availability toggled: {}, new status: {}", menuId, menu.getIsAvailable());
        return MenuResponseDto.fromWithFormattedPrice(menu, 
                priceCalculationService.formatPrice(menu.getPrice()));
    }

    @Override
    @Transactional
    public MenuResponseDto toggleMenuRecommendation(Long storeId, Long menuId, Long ownerId) {
        log.info("Toggling menu recommendation: {}, store: {}, owner: {}", menuId, storeId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴 조회
        Menu menu = getMenuByStoreAndId(storeId, menuId);

        // 추천 여부 토글
        menu.setRecommended(!menu.getIsRecommended());

        log.info("Menu recommendation toggled: {}, new status: {}", menuId, menu.getIsRecommended());
        return MenuResponseDto.fromWithFormattedPrice(menu, 
                priceCalculationService.formatPrice(menu.getPrice()));
    }

    @Override
    @Transactional
    public List<MenuResponseDto> updateDisplayOrder(Long storeId, Long categoryId, List<Long> menuIds, Long ownerId) {
        log.info("Updating display order for store: {}, category: {}, owner: {}", storeId, categoryId, ownerId);

        // 음식점 조회 및 권한 확인
        getStoreAndValidateOwnership(storeId, ownerId);

        // 메뉴들 조회 및 카테고리 확인
        List<Menu> menus = menuIds.stream()
                .map(menuId -> {
                    Menu menu = getMenuByStoreAndId(storeId, menuId);
                    if (!menu.getCategory().getId().equals(categoryId)) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "메뉴가 해당 카테고리에 속하지 않습니다.");
                    }
                    return menu;
                })
                .toList();

        // 표시 순서 업데이트
        IntStream.range(0, menus.size())
                .forEach(i -> menus.get(i).updateDisplayOrder(i));

        log.info("Display order updated for {} menus", menus.size());

        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public List<MenuResponseDto> getRecommendedMenus(Long storeId) {
        log.info("Getting recommended menus for store: {}", storeId);

        List<Menu> menus = menuRepository.findRecommendedByStoreId(storeId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
                .toList();
    }

    @Override
    public List<MenuResponseDto> getPopularMenus(Long storeId) {
        log.info("Getting popular menus for store: {}", storeId);

        List<Menu> menus = menuRepository.findPopularByStoreId(storeId);
        return menus.stream()
                .map(menu -> MenuResponseDto.fromWithFormattedPrice(menu, 
                        priceCalculationService.formatPrice(menu.getPrice())))
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

    private Menu getMenuByStoreAndId(Long storeId, Long menuId) {
        return menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private MenuCategory getMenuCategoryByStoreAndId(Long storeId, Long categoryId) {
        return menuCategoryRepository.findByIdAndStoreId(categoryId, storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_CATEGORY_NOT_FOUND));
    }
} 