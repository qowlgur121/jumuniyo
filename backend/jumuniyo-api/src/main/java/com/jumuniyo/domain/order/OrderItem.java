package com.jumuniyo.domain.order;

import com.jumuniyo.domain.BaseTimeEntity;
import com.jumuniyo.domain.menu.Menu;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 항목 엔티티
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String specialInstructions;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemOption> options = new ArrayList<>();

    @Builder
    public OrderItem(Order order, Menu menu, Integer quantity, BigDecimal unitPrice, String specialInstructions) {
        this.order = order;
        this.menu = menu;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.specialInstructions = specialInstructions;
        this.options = new ArrayList<>();
        calculateTotalPrice();
    }

    /**
     * 총 가격 계산 (단가 × 수량 + 옵션 가격)
     */
    public BigDecimal getTotalPrice() {
        BigDecimal basePrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal optionsPrice = options.stream()
            .map(option -> option.getOptionPrice() != null ? option.getOptionPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalPrice = basePrice.add(optionsPrice);
        return this.totalPrice;
    }

    /**
     * 주문 항목 옵션 추가
     */
    public void addOrderItemOption(OrderItemOption option) {
        if (options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
        option.setOrderItem(this);
        calculateTotalPrice(); // 옵션 추가 시 총 가격 재계산
    }

    /**
     * 주문 항목 옵션 제거
     */
    public void removeOrderItemOption(OrderItemOption option) {
        if (options != null) {
            options.remove(option);
            option.setOrderItem(null);
            calculateTotalPrice(); // 옵션 제거 시 총 가격 재계산
        }
    }

    /**
     * 총 가격 계산 및 설정
     */
    private void calculateTotalPrice() {
        this.totalPrice = getTotalPrice();
    }

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity != null && newQuantity > 0) {
            this.quantity = newQuantity;
            calculateTotalPrice();
        }
    }

    /**
     * 단가 변경
     */
    public void updateUnitPrice(BigDecimal newUnitPrice) {
        if (newUnitPrice != null && newUnitPrice.compareTo(BigDecimal.ZERO) >= 0) {
            this.unitPrice = newUnitPrice;
            calculateTotalPrice();
        }
    }
} 