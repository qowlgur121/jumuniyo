package com.jumuniyo.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 항목 옵션 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 항목 옵션 생성 요청 정보")
public class OrderItemOptionCreateRequestDto {

    @NotNull(message = "메뉴 옵션 ID는 필수입니다")
    @Schema(description = "메뉴 옵션 ID", example = "1")
    private Long menuOptionId;

    @Size(max = 100, message = "옵션 값은 100자 이하여야 합니다")
    @Schema(description = "선택된 옵션 값", example = "라지 사이즈")
    private String optionValue;
} 