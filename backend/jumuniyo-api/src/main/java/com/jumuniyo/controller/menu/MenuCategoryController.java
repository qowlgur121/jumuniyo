package com.jumuniyo.controller.menu;

import com.jumuniyo.dto.menu.MenuCategoryRequestDto;
import com.jumuniyo.dto.menu.MenuCategoryResponseDto;
import com.jumuniyo.service.menu.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/stores/{storeId}/menu-categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    @PostMapping
    public ResponseEntity<MenuCategoryResponseDto> createMenuCategory(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuCategoryRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuCategoryResponseDto response = menuCategoryService.createMenuCategory(storeId, requestDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<MenuCategoryResponseDto> updateMenuCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @Valid @RequestBody MenuCategoryRequestDto requestDto,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuCategoryResponseDto response = menuCategoryService.updateMenuCategory(storeId, categoryId, requestDto, ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteMenuCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        menuCategoryService.deleteMenuCategory(storeId, categoryId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MenuCategoryResponseDto>> getMenuCategories(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        
        List<MenuCategoryResponseDto> response = activeOnly 
            ? menuCategoryService.getActiveMenuCategoriesByStore(storeId)
            : menuCategoryService.getMenuCategoriesByStore(storeId);
            
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<MenuCategoryResponseDto> getMenuCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        
        MenuCategoryResponseDto response = menuCategoryService.getMenuCategory(storeId, categoryId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/toggle-status")
    public ResponseEntity<MenuCategoryResponseDto> toggleMenuCategoryStatus(
            @PathVariable Long storeId,
            @PathVariable Long categoryId,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        MenuCategoryResponseDto response = menuCategoryService.toggleMenuCategoryStatus(storeId, categoryId, ownerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/display-order")
    public ResponseEntity<List<MenuCategoryResponseDto>> updateDisplayOrder(
            @PathVariable Long storeId,
            @RequestBody List<Long> categoryIds,
            @RequestHeader("X-Owner-Id") Long ownerId) {
        
        List<MenuCategoryResponseDto> response = menuCategoryService.updateDisplayOrder(storeId, categoryIds, ownerId);
        return ResponseEntity.ok(response);
    }
} 