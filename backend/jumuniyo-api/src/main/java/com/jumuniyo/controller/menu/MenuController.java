package com.jumuniyo.controller.menu;

import com.jumuniyo.dto.menu.MenuRequestDto;
import com.jumuniyo.dto.menu.MenuResponseDto;
import com.jumuniyo.service.menu.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<MenuResponseDto> createMenu(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.createMenu(storeId, requestDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/with-image")
    public ResponseEntity<MenuResponseDto> createMenuWithImage(
            @PathVariable Long storeId,
            @RequestPart("menu") String menuJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        try {
            // JSON 문자열을 MenuRequestDto로 변환
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            MenuRequestDto requestDto = objectMapper.readValue(menuJson, MenuRequestDto.class);
            
            MenuResponseDto response = menuService.createMenuWithImage(storeId, requestDto, imageFile, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("메뉴 JSON 파싱 오류: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestBody MenuRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.updateMenu(storeId, menuId, requestDto, ownerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{menuId}/image")
    public ResponseEntity<MenuResponseDto> updateMenuImage(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestPart("image") MultipartFile imageFile,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.updateMenuImage(storeId, menuId, imageFile, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        menuService.deleteMenu(storeId, menuId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
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
    public ResponseEntity<MenuResponseDto> getMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId) {
        
        MenuResponseDto response = menuService.getMenu(storeId, menuId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuId}/toggle-availability")
    public ResponseEntity<MenuResponseDto> toggleMenuAvailability(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.toggleMenuAvailability(storeId, menuId, ownerId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuId}/toggle-recommendation")
    public ResponseEntity<MenuResponseDto> toggleMenuRecommendation(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuResponseDto response = menuService.toggleMenuRecommendation(storeId, menuId, ownerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/categories/{categoryId}/display-order")
    public ResponseEntity<List<MenuResponseDto>> updateDisplayOrder(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @RequestBody List<Long> menuIds,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        List<MenuResponseDto> response = menuService.updateDisplayOrder(storeId, categoryId, menuIds, ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<MenuResponseDto>> getRecommendedMenus(
            @PathVariable Long storeId) {
        
        List<MenuResponseDto> response = menuService.getRecommendedMenus(storeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MenuResponseDto>> getPopularMenus(
            @PathVariable Long storeId) {
        
        List<MenuResponseDto> response = menuService.getPopularMenus(storeId);
        return ResponseEntity.ok(response);
    }
} 