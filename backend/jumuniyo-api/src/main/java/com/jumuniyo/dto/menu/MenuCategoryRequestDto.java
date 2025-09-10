package com.jumuniyo.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuCategoryRequestDto {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
    private String name;

    @Size(max = 200, message = "카테고리 설명은 200자 이하여야 합니다.")
    private String description;

    private Integer displayOrder;

    // 생성 요청용 생성자
    public static MenuCategoryRequestDto of(String name, String description, Integer displayOrder) {
        return MenuCategoryRequestDto.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .build();
    }
} 