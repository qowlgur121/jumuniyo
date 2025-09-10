package com.jumuniyo.dto.order;

import com.jumuniyo.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 상태 업데이트 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 상태 업데이트 요청 정보")
public class OrderStatusUpdateRequestDto {

    @NotNull(message = "주문 상태는 필수입니다")
    @Schema(description = "변경할 주문 상태", example = "CONFIRMED")
    private OrderStatus status;

    @Size(max = 500, message = "상태 변경 사유는 500자 이하여야 합니다")
    @Schema(description = "상태 변경 사유", example = "고객 요청에 의한 주문 취소")
    private String reason;
} 