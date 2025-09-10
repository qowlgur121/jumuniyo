package com.jumuniyo.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 주문 항목 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 항목 생성 요청 정보")
public class OrderItemCreateRequestDto {

    @NotNull(message = "메뉴 ID는 필수입니다")
    @Schema(description = "메뉴 ID", example = "1")
    private Long menuId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Schema(description = "주문 수량", example = "2")
    private Integer quantity;

    @Size(max = 500, message = "특별 요청사항은 500자 이하여야 합니다")
    @Schema(description = "특별 요청사항", example = "덜 맵게 해주세요")
    private String specialRequests;

    @Valid
    @Schema(description = "선택된 옵션 목록")
    private List<OrderItemOptionCreateRequestDto> options;
} 