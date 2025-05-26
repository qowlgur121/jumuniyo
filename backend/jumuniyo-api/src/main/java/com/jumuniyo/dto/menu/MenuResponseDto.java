package com.jumuniyo.dto.menu;

import com.jumuniyo.domain.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponseDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String formattedPrice; // 포맷팅된 가격 (예: "15,000원")
    private String imageUrl;
    private Boolean isAvailable;
    private Boolean isRecommended;
    private Integer displayOrder;
    private Integer soldCount;
    private Long storeId;
    private Long categoryId;
    private String categoryName;
    private Integer optionGroupCount; // 옵션 그룹 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public static MenuResponseDto from(Menu menu) {
        return MenuResponseDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .isAvailable(menu.getIsAvailable())
                .isRecommended(menu.getIsRecommended())
                .displayOrder(menu.getDisplayOrder())
                .soldCount(menu.getSoldCount())
                .storeId(menu.getStore().getId())
                .categoryId(menu.getCategory().getId())
                .categoryName(menu.getCategory().getName())
                .optionGroupCount(menu.getOptionGroups().size())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    // Entity -> DTO 변환 (포맷팅된 가격 포함)
    public static MenuResponseDto fromWithFormattedPrice(Menu menu, String formattedPrice) {
        return MenuResponseDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .formattedPrice(formattedPrice)
                .imageUrl(menu.getImageUrl())
                .isAvailable(menu.getIsAvailable())
                .isRecommended(menu.getIsRecommended())
                .displayOrder(menu.getDisplayOrder())
                .soldCount(menu.getSoldCount())
                .storeId(menu.getStore().getId())
                .categoryId(menu.getCategory().getId())
                .categoryName(menu.getCategory().getName())
                .optionGroupCount(menu.getOptionGroups().size())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }

    // 간단한 메뉴 정보용 DTO (목록 조회시 사용)
    public static MenuResponseDto simple(Menu menu) {
        return MenuResponseDto.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .imageUrl(menu.getImageUrl())
                .isAvailable(menu.getIsAvailable())
                .isRecommended(menu.getIsRecommended())
                .displayOrder(menu.getDisplayOrder())
                .soldCount(menu.getSoldCount())
                .categoryId(menu.getCategory().getId())
                .categoryName(menu.getCategory().getName())
                .build();
    }
} 