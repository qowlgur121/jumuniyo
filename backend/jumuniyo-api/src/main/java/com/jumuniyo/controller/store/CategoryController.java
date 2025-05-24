package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.CategoryResponseDto;
import com.jumuniyo.service.store.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 활성화된 카테고리 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategories() {
        log.info("활성화된 카테고리 목록 조회 요청");
        
        List<CategoryResponseDto> categories = categoryService.getActiveCategories()
                .stream()
                .map(CategoryResponseDto::from)
                .toList();
        
        return ResponseEntity.ok(categories);
    }

    /**
     * 카테고리 상세 조회
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable Long categoryId) {
        log.info("카테고리 조회 요청: ID={}", categoryId);
        
        CategoryResponseDto category = CategoryResponseDto.from(
                categoryService.getCategory(categoryId)
        );
        
        return ResponseEntity.ok(category);
    }
} 