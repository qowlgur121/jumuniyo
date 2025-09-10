package com.jumuniyo.controller.store;

import com.jumuniyo.dto.menu.MenuResponseDto;
import com.jumuniyo.dto.store.PublicStoreResponseDto;
import com.jumuniyo.service.menu.MenuService;
import com.jumuniyo.service.store.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/stores")
@RequiredArgsConstructor
@Tag(name = "Public Store API", description = "공개 매장 정보 조회 API")
public class PublicStoreController {

    private final StoreService storeService;
    private final MenuService menuService;

    @Operation(summary = "모든 매장 목록 조회 (페이징)", description = "승인된 모든 매장 목록을 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", schema = @Schema(type = "integer", defaultValue = "10"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매장 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))) // 실제로는 Page<PublicStoreResponseDto>를 반환
    })
    @GetMapping
    public ResponseEntity<Page<PublicStoreResponseDto>> getAllStores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all stores with pagination: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicStoreResponseDto> stores = storeService.findAllApprovedStores(pageable);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "특정 매장 상세 정보 조회", description = "매장 ID를 사용하여 특정 매장의 상세 정보를 조회합니다.")
    @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매장 상세 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PublicStoreResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{storeIdx}")
    public ResponseEntity<PublicStoreResponseDto> getStoreById(@PathVariable Long storeIdx) {
        log.info("Fetching store by ID: {}", storeIdx);
        PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(store);
    }

    @Operation(summary = "카테고리별 매장 목록 조회 (페이징)", description = "특정 카테고리에 속하는 승인된 매장 목록을 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "categoryName", description = "카테고리명", required = true, example = "치킨"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", schema = @Schema(type = "integer", defaultValue = "10"))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 매장 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))), // 실제로는 Page<PublicStoreResponseDto>를 반환
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content) // 이 경우는 서비스에서 처리할 수 있음
    })
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Page<PublicStoreResponseDto>> getStoresByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching stores by category: {}, pagination: page={}, size={}", categoryName, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicStoreResponseDto> stores = storeService.findApprovedStoresByCategory(categoryName, pageable);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "특정 매장의 모든 메뉴 조회", description = "매장 ID를 사용하여 해당 매장의 모든 메뉴 목록을 조회합니다.")
    @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))), // 실제로는 List<MenuResponseDto>를 반환
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음", content = @Content) // 매장 존재 여부 서비스에서 확인 가능
    })
    @GetMapping("/{storeIdx}/menus")
    public ResponseEntity<List<MenuResponseDto>> getMenusByStore(@PathVariable Long storeIdx) {
        log.info("Fetching menus for store ID: {}", storeIdx);
        // 매장 존재 여부 확인 (선택 사항, 서비스 레이어에서 처리 권장)
        // PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        // if (store == null) {
        // return ResponseEntity.notFound().build();
        // }
        List<MenuResponseDto> menus = menuService.getMenusByStore(storeIdx); // 기존 getMenusByStore 사용
        return ResponseEntity.ok(menus);
    }

    @Operation(summary = "매장 이름으로 검색 (페이징)", description = "매장 이름에 검색어가 포함된 승인된 매장 목록을 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "storeName", description = "검색할 매장 이름", required = true, example = "불닭"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", schema = @Schema(type = "integer", defaultValue = "10"))
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "매장 검색 성공",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<Page<PublicStoreResponseDto>> searchStoresByName(
            @RequestParam String storeName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching stores by name: {}, pagination: page={}, size={}", storeName, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicStoreResponseDto> stores = storeService.searchApprovedStoresByName(storeName, pageable);
        return ResponseEntity.ok(stores);
    }

    @Operation(summary = "위치 기반 매장 검색 (페이징)", description = "사용자 위치(위도, 경도)와 검색 반경(km) 내의 승인된 매장 목록을 페이지네이션하여 조회합니다.")
    @Parameters({
            @Parameter(name = "latitude", description = "사용자 위도", required = true, example = "37.5665"),
            @Parameter(name = "longitude", description = "사용자 경도", required = true, example = "126.9780"),
            @Parameter(name = "radiusKm", description = "검색 반경(km)", required = true, example = "3.0"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0", schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "페이지 당 항목 수", example = "10", schema = @Schema(type = "integer", defaultValue = "10"))
    })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "위치 기반 매장 검색 성공",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/location")
    public ResponseEntity<Page<PublicStoreResponseDto>> findStoresByLocation(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Finding stores by location: lat={}, lon={}, radiusKm={}, page={}, size={}", latitude, longitude, radiusKm, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicStoreResponseDto> stores = storeService.findApprovedStoresByLocation(latitude, longitude, radiusKm, pageable);
        return ResponseEntity.ok(stores);
    }

    // 아래는 기존의 복잡한 필터링 및 추천/인기 메뉴 조회 엔드포인트들입니다.
    // 필요시 Swagger 어노테이션을 추가하고, 서비스 로직을 명확히 연결해야 합니다.
    // 우선 기본적인 CRUD 및 검색 기능의 컴파일 오류 해결에 집중합니다.

    /*
    // 일반 사용자용 가게 목록 조회 (승인된 가게만) - 더 복잡한 필터 포함 가능
    @GetMapping("/search/filtered") // 엔드포인트 변경 예시
    public ResponseEntity<Page<PublicStoreResponseDto>> getPublicStoresFiltered(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String area,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") Boolean approvedOnly, // Public API에서는 항상 true
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal maxMinimumOrderAmount,
            @RequestParam(required = false) BigDecimal maxDeliveryFee,
            @RequestParam(required = false) Integer maxDeliveryTime,
            @RequestParam(required = false) Integer minReviewCount,
            @RequestParam(defaultValue = "false") Boolean freeDeliveryOnly,
            @RequestParam(defaultValue = "false") Boolean newStoreOnly) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PublicStoreResponseDto> stores = storeService.searchPublicStores( // 기존 서비스 메소드 활용
                keyword, categoryId, area, sortBy, true, // approvedOnly = true 강제
                minRating, maxMinimumOrderAmount, maxDeliveryFee, maxDeliveryTime,
                minReviewCount, freeDeliveryOnly, newStoreOnly, pageable);
        return ResponseEntity.ok(stores);
    }

    // 일반 사용자용 가게 메뉴 목록 조회 (승인된 가게의 사용 가능한 메뉴만)
    @GetMapping("/{storeIdx}/menus/available") // 명확한 엔드포인트
    @Operation(summary = "특정 매장의 판매 가능한 메뉴 조회", description = "매장 ID를 사용하여 해당 매장의 판매 가능한 메뉴 목록을 조회합니다.")
    @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1")
    // ... (ApiResponses 추가)
    public ResponseEntity<List<MenuResponseDto>> getPublicStoreAvailableMenus(
            @PathVariable Long storeIdx,
            @RequestParam(required = false) Long menuCategoryId) { // 메뉴 카테고리별 필터링 추가 가능성
        // 매장 존재 및 승인 여부 확인
        PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<MenuResponseDto> menus;
        if (menuCategoryId != null) {
            menus = menuService.getAvailableMenusByCategory(menuCategoryId); // 특정 가게의 특정 메뉴 카테고리 필터링 필요
        } else {
            menus = menuService.getAvailableMenusByStore(storeIdx);
        }
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{storeIdx}/menus/recommended")
    @Operation(summary = "특정 매장의 추천 메뉴 조회", description = "매장 ID를 사용하여 해당 매장의 추천 메뉴 목록을 조회합니다.")
    @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1")
    // ... (ApiResponses 추가)
    public ResponseEntity<List<MenuResponseDto>> getPublicStoreRecommendedMenus(@PathVariable Long storeIdx) {
        PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        List<MenuResponseDto> menus = menuService.getRecommendedMenus(storeIdx);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{storeIdx}/menus/popular")
    @Operation(summary = "특정 매장의 인기 메뉴 조회", description = "매장 ID를 사용하여 해당 매장의 인기 메뉴 목록을 조회합니다.")
    @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1")
    // ... (ApiResponses 추가)
    public ResponseEntity<List<MenuResponseDto>> getPublicStorePopularMenus(@PathVariable Long storeIdx) {
        PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        List<MenuResponseDto> menus = menuService.getPopularMenus(storeIdx);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{storeIdx}/menus/{menuId}")
    @Operation(summary = "특정 매장의 특정 메뉴 상세 조회", description = "매장 ID와 메뉴 ID를 사용하여 특정 메뉴의 상세 정보를 조회합니다.")
    @Parameters({
        @Parameter(name = "storeIdx", description = "매장 ID", required = true, example = "1"),
        @Parameter(name = "menuId", description = "메뉴 ID", required = true, example = "101")
    })
    // ... (ApiResponses 추가)
    public ResponseEntity<MenuResponseDto> getPublicStoreMenu(
            @PathVariable Long storeIdx, 
            @PathVariable Long menuId) {
        PublicStoreResponseDto store = storeService.findApprovedStoreById(storeIdx);
        if (store == null) {
            return ResponseEntity.notFound().build();
        }
        MenuResponseDto menu = menuService.getMenu(storeIdx, menuId); // storeIdx는 메뉴 권한/소속 확인용
        if (menu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(menu);
    }
    */
} 