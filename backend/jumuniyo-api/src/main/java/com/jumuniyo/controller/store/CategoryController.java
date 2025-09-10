package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.CategoryResponseDto;
import com.jumuniyo.service.store.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "카테고리 API", description = "매장 카테고리 조회 관련 API")
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
        summary = "활성화된 카테고리 목록 조회",
        description = "현재 활성화된 모든 카테고리 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getActiveCategories() {
        log.info("활성화된 카테고리 목록 조회 요청");
        
        List<CategoryResponseDto> categories = categoryService.getActiveCategories()
                .stream()
                .map(CategoryResponseDto::from)
                .toList();
        
        return ResponseEntity.ok(categories);
    }

    @Operation(
        summary = "카테고리 상세 조회",
        description = "특정 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 조회 성공"),
        @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategory(
            @Parameter(description = "카테고리 ID", required = true, example = "1")
            @PathVariable Long categoryId) {
        log.info("카테고리 조회 요청: ID={}", categoryId);
        
        CategoryResponseDto category = CategoryResponseDto.from(
                categoryService.getCategory(categoryId)
        );
        
        return ResponseEntity.ok(category);
    }
} 