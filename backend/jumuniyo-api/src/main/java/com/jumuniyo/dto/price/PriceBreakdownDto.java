package com.jumuniyo.dto.price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceBreakdownDto {

    private BigDecimal basePrice; // 메뉴 기본 가격
    private List<OptionPriceDto> optionPrices; // 선택된 옵션들의 가격 정보
    private BigDecimal totalOptionsPrice; // 옵션 총 가격
    private BigDecimal subtotal; // 소계 (기본 가격 + 옵션 가격)
    private BigDecimal discountAmount; // 할인 금액 (향후 확장용)
    private BigDecimal taxAmount; // 세금 (향후 확장용)
    private BigDecimal totalPrice; // 최종 총 가격
    private String formattedTotalPrice; // 포맷팅된 총 가격 (예: "15,000원")

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionPriceDto {
        private Long optionId;
        private String optionName;
        private BigDecimal additionalPrice;
        private String formattedPrice;
    }

    // 정적 팩토리 메서드
    public static PriceBreakdownDto of(BigDecimal basePrice, List<OptionPriceDto> optionPrices, 
                                       BigDecimal totalOptionsPrice, BigDecimal totalPrice, 
                                       String formattedTotalPrice) {
        return PriceBreakdownDto.builder()
                .basePrice(basePrice)
                .optionPrices(optionPrices)
                .totalOptionsPrice(totalOptionsPrice)
                .subtotal(basePrice.add(totalOptionsPrice))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalPrice(totalPrice)
                .formattedTotalPrice(formattedTotalPrice)
                .build();
    }
} 