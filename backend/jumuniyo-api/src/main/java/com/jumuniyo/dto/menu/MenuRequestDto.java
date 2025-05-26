package com.jumuniyo.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRequestDto {

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 100, message = "메뉴 이름은 100자 이하여야 합니다.")
    private String name;

    @Size(max = 500, message = "메뉴 설명은 500자 이하여야 합니다.")
    private String description;

    @NotNull(message = "메뉴 가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "메뉴 가격은 0보다 커야 합니다.")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "메뉴 카테고리는 필수입니다.")
    private Long categoryId;

    private Boolean isRecommended;

    private Integer displayOrder;

    // 생성 요청용 정적 팩토리 메서드
    public static MenuRequestDto of(String name, String description, BigDecimal price, 
                                   Long categoryId, Boolean isRecommended, Integer displayOrder) {
        return MenuRequestDto.builder()
                .name(name)
                .description(description)
                .price(price)
                .categoryId(categoryId)
                .isRecommended(isRecommended)
                .displayOrder(displayOrder)
                .build();
    }

    // 이미지 포함 생성 요청용 정적 팩토리 메서드
    public static MenuRequestDto withImage(String name, String description, BigDecimal price, 
                                          String imageUrl, Long categoryId, Boolean isRecommended, Integer displayOrder) {
        return MenuRequestDto.builder()
                .name(name)
                .description(description)
                .price(price)
                .imageUrl(imageUrl)
                .categoryId(categoryId)
                .isRecommended(isRecommended)
                .displayOrder(displayOrder)
                .build();
    }
} 