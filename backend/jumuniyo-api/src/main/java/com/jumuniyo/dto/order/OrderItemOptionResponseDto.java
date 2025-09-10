package com.jumuniyo.dto.order;

import com.jumuniyo.domain.order.OrderItemOption;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 주문 항목 옵션 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 항목 옵션 응답 정보")
public class OrderItemOptionResponseDto {

    @Schema(description = "주문 항목 옵션 ID", example = "1")
    private Long id;

    @Schema(description = "메뉴 옵션 ID", example = "1")
    private Long menuOptionId;

    @Schema(description = "옵션 그룹명", example = "사이즈")
    private String optionGroupName;

    @Schema(description = "옵션명", example = "라지")
    private String optionName;

    @Schema(description = "선택된 옵션 값", example = "라지 사이즈")
    private String optionValue;

    @Schema(description = "추가 가격", example = "1000")
    private BigDecimal additionalPrice;

    /**
     * OrderItemOption 엔티티를 OrderItemOptionResponseDto로 변환
     */
    public static OrderItemOptionResponseDto from(OrderItemOption orderItemOption) {
        if (orderItemOption == null) {
            return null;
        }

        OrderItemOptionResponseDto dto = new OrderItemOptionResponseDto();
        dto.setId(orderItemOption.getId());
        dto.setOptionValue(orderItemOption.getOptionValue());
        dto.setAdditionalPrice(orderItemOption.getAdditionalPrice());

        // 메뉴 옵션 정보
        if (orderItemOption.getMenuOption() != null) {
            dto.setMenuOptionId(orderItemOption.getMenuOption().getId());
            dto.setOptionName(orderItemOption.getMenuOption().getName());
            
            // 옵션 그룹 정보
            if (orderItemOption.getMenuOption().getOptionGroup() != null) {
                dto.setOptionGroupName(orderItemOption.getMenuOption().getOptionGroup().getName());
            }
        }

        return dto;
    }
} 