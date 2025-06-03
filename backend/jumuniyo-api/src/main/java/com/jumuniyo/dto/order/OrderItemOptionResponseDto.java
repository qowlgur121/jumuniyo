package com.jumuniyo.dto.order;

import com.jumuniyo.domain.order.OrderItemOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionResponseDto {
    private Long id;
    private String optionGroupName;
    private String optionName;
    private BigDecimal optionPrice;
    private String displayText; // 예: "맵기 선택: 매운맛 (+0원)"

    public static OrderItemOptionResponseDto fromEntity(OrderItemOption option) {
        String text = option.getOptionGroupName() + ": " + option.getOptionName();
        if (option.getOptionPrice() != null && option.getOptionPrice().compareTo(BigDecimal.ZERO) > 0) {
            text += " (+" + option.getOptionPrice().toPlainString() + "원)";
        } else {
            text += " (+0원)"; // 가격이 0이거나 null이면 (+0원)으로 표시 (선택사항)
        }

        return OrderItemOptionResponseDto.builder()
                .id(option.getId())
                .optionGroupName(option.getOptionGroupName())
                .optionName(option.getOptionName())
                .optionPrice(option.getOptionPrice())
                .displayText(text) // 생성된 텍스트 할당
                .build();
    }
} 