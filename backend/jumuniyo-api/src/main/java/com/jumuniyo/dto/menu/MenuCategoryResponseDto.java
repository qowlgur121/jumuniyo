package com.jumuniyo.dto.menu;

import com.jumuniyo.domain.menu.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategoryResponseDto {

    private Long id;
    private String name;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private Long storeId;
    private Integer menuCount; // 해당 카테고리의 메뉴 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public static MenuCategoryResponseDto from(MenuCategory menuCategory) {
        return MenuCategoryResponseDto.builder()
                .id(menuCategory.getId())
                .name(menuCategory.getName())
                .description(menuCategory.getDescription())
                .displayOrder(menuCategory.getDisplayOrder())
                .isActive(menuCategory.getIsActive())
                .storeId(menuCategory.getStore().getId())
                .menuCount(menuCategory.getMenus().size())
                .createdAt(menuCategory.getCreatedAt())
                .updatedAt(menuCategory.getUpdatedAt())
                .build();
    }

    // Entity -> DTO 변환 (메뉴 개수 별도 제공)
    public static MenuCategoryResponseDto from(MenuCategory menuCategory, Integer menuCount) {
        return MenuCategoryResponseDto.builder()
                .id(menuCategory.getId())
                .name(menuCategory.getName())
                .description(menuCategory.getDescription())
                .displayOrder(menuCategory.getDisplayOrder())
                .isActive(menuCategory.getIsActive())
                .storeId(menuCategory.getStore().getId())
                .menuCount(menuCount)
                .createdAt(menuCategory.getCreatedAt())
                .updatedAt(menuCategory.getUpdatedAt())
                .build();
    }
} 