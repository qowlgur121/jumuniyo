package com.jumuniyo.dto.order;

import com.jumuniyo.domain.order.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 항목 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 항목 응답 정보")
public class OrderItemResponseDto {

    @Schema(description = "주문 항목 ID", example = "1")
    private Long id;

    @Schema(description = "메뉴 ID", example = "1")
    private Long menuId;

    @Schema(description = "메뉴명", example = "짜장면")
    private String menuName;

    @Schema(description = "메뉴 설명", example = "정통 중화식 짜장면")
    private String menuDescription;

    @Schema(description = "수량", example = "2")
    private Integer quantity;

    @Schema(description = "단가", example = "8000")
    private BigDecimal unitPrice;

    @Schema(description = "총 금액", example = "16000")
    private BigDecimal totalPrice;

    @Schema(description = "특별 요청사항", example = "덜 맵게 해주세요")
    private String specialInstructions;

    @Schema(description = "주문 항목 옵션 목록")
    private List<OrderItemOptionResponseDto> options;

    /**
     * OrderItem 엔티티를 OrderItemResponseDto로 변환
     */
    public static OrderItemResponseDto from(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());
        dto.setSpecialInstructions(orderItem.getSpecialInstructions());

        // 메뉴 정보
        if (orderItem.getMenu() != null) {
            dto.setMenuId(orderItem.getMenu().getId());
            dto.setMenuName(orderItem.getMenu().getName());
            dto.setMenuDescription(orderItem.getMenu().getDescription());
        }

        // 옵션 정보
        if (orderItem.getOptions() != null) {
            dto.setOptions(
                orderItem.getOptions().stream()
                    .map(OrderItemOptionResponseDto::from)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
} 