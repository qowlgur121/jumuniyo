package com.jumuniyo.domain.order;

import com.jumuniyo.domain.BaseTimeEntity;
import com.jumuniyo.domain.menu.MenuOption;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 주문 항목 옵션 엔티티
 */
@Entity
@Table(name = "order_item_options")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOption menuOption;

    @Column(length = 100)
    private String optionValue;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Builder
    public OrderItemOption(OrderItem orderItem, MenuOption menuOption, String optionValue, BigDecimal additionalPrice) {
        this.orderItem = orderItem;
        this.menuOption = menuOption;
        this.optionValue = optionValue;
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
    }

    /**
     * 옵션 가격을 반환합니다 (additionalPrice 별칭)
     */
    public BigDecimal getOptionPrice() {
        return this.additionalPrice;
    }

    /**
     * 옵션 그룹명을 반환합니다 (MenuOption에서 가져옴)
     */
    public String getOptionGroupName() {
        return menuOption != null && menuOption.getOptionGroup() != null 
            ? menuOption.getOptionGroup().getName() 
            : "기타";
    }

    /**
     * 옵션명을 반환합니다 (MenuOption에서 가져옴)
     */
    public String getOptionName() {
        return menuOption != null ? menuOption.getName() : "알 수 없는 옵션";
    }

    /**
     * 옵션 가격 설정
     */
    public void setOptionPrice(BigDecimal optionPrice) {
        this.additionalPrice = optionPrice != null ? optionPrice : BigDecimal.ZERO;
    }

    /**
     * 표시용 텍스트 반환
     */
    public String getDisplayText() {
        String text = getOptionGroupName() + ": " + getOptionName();
        if (optionValue != null && !optionValue.isEmpty()) {
            text += " (" + optionValue + ")";
        }
        if (additionalPrice != null && additionalPrice.compareTo(BigDecimal.ZERO) > 0) {
            text += " (+" + additionalPrice.toPlainString() + "원)";
        }
        return text;
    }
} 