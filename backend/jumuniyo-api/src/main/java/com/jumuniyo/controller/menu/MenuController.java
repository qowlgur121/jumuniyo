package com.jumuniyo.controller.menu;

import com.jumuniyo.dto.menu.MenuRequestDto;
import com.jumuniyo.dto.menu.MenuResponseDto;
import com.jumuniyo.service.menu.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
@Tag(name = "메뉴 관리 API (사업자용)", description = "특정 매장의 메뉴 생성, 수정, 삭제, 조회 등 관리 기능 API (사업자 인증 필요 - X-Owner-Id 헤더 사용)")
// @SecurityRequirement(name = "bearerAuth") // JWT 사용 시 주석 해제
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @Operation(summary = "신규 메뉴 생성", description = "매장에 새로운 메뉴를 등록합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "메뉴를 등록할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "생성할 메뉴 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = MenuRequestDto.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메뉴 생성 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (X-Owner-Id 누락 또는 유효하지 않음)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (해당 매장 소유주가 아님)"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> createMenu(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.createMenu(storeId, requestDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지와 함께 신규 메뉴 생성", description = "매장에 새로운 메뉴를 이미지와 함께 등록합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "메뉴를 등록할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    // RequestBody 대신 RequestPart를 사용하므로, 각 파트를 @Parameter로 설명하거나 Swagger 어노테이션 없이 SpringDoc이 자동 추론하도록 둘 수 있습니다.
    // 여기서는 명시적으로 menuJson과 imageFile을 설명합니다.
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "메뉴 정보(JSON)와 이미지 파일(Multipart)",
        required = true,
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, 
                           schema = @Schema(type = "object"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "메뉴 생성 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (JSON 파싱 오류, 이미지 업로드 실패 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> createMenuWithImage(
            @PathVariable Long storeId,
            @Parameter(description = "메뉴 정보 JSON (MenuRequestDto 형식)") @RequestPart("menu") String menuJson, // Swagger UI에서 직접 테스트하기 어려울 수 있음
            @Parameter(description = "메뉴 이미지 파일") @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            MenuRequestDto requestDto = objectMapper.readValue(menuJson, MenuRequestDto.class);
            
            MenuResponseDto response = menuService.createMenuWithImage(storeId, requestDto, imageFile, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("메뉴 JSON 파싱 오류 또는 이미지 처리 오류: ", e);
            return ResponseEntity.badRequest().build(); // 실제로는 더 구체적인 오류 응답 필요
        }
    }

    @PutMapping("/{menuId}")
    @Operation(summary = "메뉴 정보 수정", description = "기존 메뉴의 정보를 수정합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "수정할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "수정할 메뉴 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = MenuRequestDto.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 수정 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.updateMenu(storeId, menuId, requestDto, ownerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{menuId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "메뉴 이미지 수정/업로드", description = "기존 메뉴의 이미지를 새로 업로드하거나 수정합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "이미지를 수정할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "새로운 메뉴 이미지 파일",
        required = true,
        content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, 
                           schema = @Schema(type = "object"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 이미지 수정 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미지 파일 누락 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> updateMenuImage(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Parameter(description = "업로드할 메뉴 이미지 파일") @RequestPart("image") MultipartFile imageFile,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.updateMenuImage(storeId, menuId, imageFile, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{menuId}")
    @Operation(summary = "메뉴 삭제", description = "매장에서 특정 메뉴를 삭제합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "삭제할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "메뉴 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        menuService.deleteMenu(storeId, menuId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "메뉴 목록 조회 (공개/사업자용)", description = "특정 매장의 메뉴 목록을 조회합니다. 판매 가능 여부 또는 카테고리별 필터링이 가능합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "메뉴 목록을 조회할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "availableOnly", description = "판매 가능한 메뉴만 조회할지 여부 (기본값: false)", schema = @Schema(type = "boolean", defaultValue = "false")),
            @Parameter(name = "categoryId", description = "특정 카테고리에 속한 메뉴만 조회 (ID)", required = false, example = "10")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 목록 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = MenuResponseDto.class)))
            // 404 (매장 없음) 등도 추가 가능
    })
    public ResponseEntity<List<MenuResponseDto>> getMenus(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(required = false) Long categoryId) {
        
        List<MenuResponseDto> response;
        
        if (categoryId != null) {
            response = availableOnly 
                ? menuService.getAvailableMenusByCategory(categoryId)
                : menuService.getMenusByCategory(categoryId);
        } else {
            response = availableOnly 
                ? menuService.getAvailableMenusByStore(storeId)
                : menuService.getMenusByStore(storeId);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{menuId}")
    @Operation(summary = "특정 메뉴 상세 조회 (공개/사업자용)", description = "특정 메뉴의 상세 정보를 조회합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "조회할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메뉴 상세 정보 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> getMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        
        MenuResponseDto response = menuService.getMenu(storeId, menuId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuId}/toggle-availability")
    @Operation(summary = "메뉴 판매 가능 상태 변경", description = "특정 메뉴의 판매 가능 상태(품절/판매중)를 토글합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "상태를 변경할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> toggleMenuAvailability(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.toggleMenuAvailability(storeId, menuId, ownerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuId}/toggle-recommendation")
    @Operation(summary = "메뉴 추천 상태 변경", description = "특정 메뉴의 추천 상태를 토글합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "menuId", description = "추천 상태를 변경할 메뉴의 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 상태 변경 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 메뉴를 찾을 수 없음")
    })
    public ResponseEntity<MenuResponseDto> toggleMenuRecommendation(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.toggleMenuRecommendation(storeId, menuId, ownerId);
        return ResponseEntity.ok(response);
    }

    // 메뉴 표시 순서 변경 API는 menuIds를 RequestBody로 받으므로 @io.swagger.v3.oas.annotations.parameters.RequestBody 사용
    @PutMapping("/categories/{categoryId}/display-order")
    @Operation(summary = "카테고리 내 메뉴 표시 순서 변경", description = "특정 카테고리 내의 메뉴들의 표시 순서를 일괄적으로 업데이트합니다.")
    @Parameters({
            @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
            @Parameter(name = "categoryId", description = "메뉴 순서를 변경할 카테고리 ID", required = true, in = ParameterIn.PATH, example = "10"),
            @Parameter(name = "X-Owner-Id", description = "인증된 사업자 ID", required = true, in = ParameterIn.HEADER, example = "1")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "새로운 메뉴 순서 (메뉴 ID 리스트)",
            required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = Long.class, example = "[3, 1, 2]"))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "표시 순서 업데이트 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (ID 목록 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "매장 또는 카테고리를 찾을 수 없음")
    })
    public ResponseEntity<List<MenuResponseDto>> updateDisplayOrder(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @RequestBody List<Long> menuIds,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        List<MenuResponseDto> response = menuService.updateDisplayOrder(storeId, categoryId, menuIds, ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended")
    @Operation(summary = "추천 메뉴 목록 조회 (공개)", description = "특정 매장의 추천 메뉴 목록을 조회합니다.")
    @Parameter(name = "storeId", description = "추천 메뉴를 조회할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 메뉴 목록 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    public ResponseEntity<List<MenuResponseDto>> getRecommendedMenus(
            @PathVariable Long storeId) {
        
        List<MenuResponseDto> response = menuService.getRecommendedMenus(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 메뉴 목록 조회 (공개)", description = "특정 매장의 인기 메뉴 목록을 조회합니다. (구현 방식에 따라 집계 필요)")
    @Parameter(name = "storeId", description = "인기 메뉴를 조회할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 메뉴 목록 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = MenuResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음")
    })
    public ResponseEntity<List<MenuResponseDto>> getPopularMenus(
            @PathVariable Long storeId) {
        
        List<MenuResponseDto> response = menuService.getPopularMenus(storeId);
        return ResponseEntity.ok(response);
    }
} 