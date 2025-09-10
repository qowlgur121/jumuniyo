package com.jumuniyo.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주문 생성 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 생성 요청 정보")
public class OrderCreateRequestDto {

    @NotNull(message = "매장 ID는 필수입니다")
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @NotBlank(message = "배달 주소는 필수입니다")
    @Size(max = 255, message = "배달 주소는 255자 이하여야 합니다")
    @Schema(description = "배달 주소", example = "서울시 강남구 테헤란로 123")
    private String deliveryAddress;

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다")
    @Schema(description = "상세 배달 주소", example = "101동 1001호")
    private String deliveryAddressDetail;

    @Size(max = 500, message = "배달 요청사항은 500자 이하여야 합니다")
    @Schema(description = "배달 요청사항", example = "문 앞에 놔주세요")
    private String deliveryRequest;

    @Size(max = 500, message = "가게 요청사항은 500자 이하여야 합니다")
    @Schema(description = "가게 요청사항", example = "덜 맵게 해주세요")
    private String storeRequest;

    @NotBlank(message = "고객 전화번호는 필수입니다")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    @Schema(description = "고객 전화번호", example = "010-1234-5678")
    private String customerPhone;

    @Size(max = 50, message = "고객 이름은 50자 이하여야 합니다")
    @Schema(description = "고객 이름", example = "홍길동")
    private String customerName;

    @NotNull(message = "총 주문 금액은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "총 금액은 0보다 커야 합니다")
    @Schema(description = "총 주문 금액", example = "25000")
    private BigDecimal totalAmount;

    @NotNull(message = "배달비는 필수입니다")
    @DecimalMin(value = "0.0", message = "배달비는 0 이상이어야 합니다")
    @Schema(description = "배달비", example = "3000")
    private BigDecimal deliveryFee;

    @NotEmpty(message = "주문 항목은 최소 1개 이상이어야 합니다")
    @Valid
    @Schema(description = "주문 항목 목록")
    private List<OrderItemCreateRequestDto> orderItems;

    /**
     * 전화번호 가져오기 (별칭 메서드)
     */
    public String getPhoneNumber() {
        return this.customerPhone;
    }

    /**
     * 주문 항목 DTO (호환성을 위한 내부 클래스)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(description = "주문 항목 정보")
    public static class OrderItemDto {
        
        @NotNull(message = "메뉴 ID는 필수입니다")
        @Schema(description = "메뉴 ID", example = "1")
        private Long menuId;

        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
        @Schema(description = "주문 수량", example = "2")
        private Integer quantity;

        @NotNull(message = "단가는 필수입니다")
        @DecimalMin(value = "0.0", inclusive = false, message = "단가는 0보다 커야 합니다")
        @Schema(description = "단가", example = "8000")
        private BigDecimal unitPrice;

        @Size(max = 500, message = "특별 요청사항은 500자 이하여야 합니다")
        @Schema(description = "특별 요청사항", example = "덜 맵게 해주세요")
        private String specialInstructions;

        @Valid
        @Schema(description = "선택된 옵션 목록")
        private List<OrderItemOptionDto> options;

        /**
         * 주문 항목 옵션 DTO
         */
        @Getter
        @Setter
        @NoArgsConstructor
        @Schema(description = "주문 항목 옵션 정보")
        public static class OrderItemOptionDto {
            
            @NotNull(message = "메뉴 옵션 ID는 필수입니다")
            @Schema(description = "메뉴 옵션 ID", example = "1")
            private Long menuOptionId;

            @Size(max = 100, message = "옵션 값은 100자 이하여야 합니다")
            @Schema(description = "선택된 옵션 값", example = "라지 사이즈")
            private String optionValue;

            @DecimalMin(value = "0.0", message = "추가 가격은 0 이상이어야 합니다")
            @Schema(description = "추가 가격", example = "1000")
            private BigDecimal additionalPrice;
        }
    }
} 