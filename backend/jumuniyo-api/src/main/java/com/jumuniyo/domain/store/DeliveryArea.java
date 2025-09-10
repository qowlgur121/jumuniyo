package com.jumuniyo.domain.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_areas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DeliveryArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_area_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String areaName; // 배달 지역 이름 (예: 강남구, 서초구 등)

    @Column(length = 255)
    private String detailAddress; // 상세 주소 (예: 특정 동, 아파트 단지 등)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO; // 해당 지역 배달비

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO; // 해당 지역 최소 주문 금액

    @Column(nullable = false)
    private Integer deliveryTimeMinutes = 30; // 예상 배달 시간 (분)

    @Column(nullable = false)
    private Boolean isActive = true; // 배달 가능 여부

    @Column(length = 500)
    private String description; // 배달 지역 설명 또는 특이사항

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @Setter // Store 엔티티에서 설정할 수 있도록 Setter 추가
    private Store store;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public DeliveryArea(String areaName, String detailAddress, BigDecimal deliveryFee, 
                       BigDecimal minimumOrderAmount, Integer deliveryTimeMinutes, 
                       Boolean isActive, String description, Store store) {
        this.areaName = areaName;
        this.detailAddress = detailAddress;
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.minimumOrderAmount = minimumOrderAmount != null ? minimumOrderAmount : BigDecimal.ZERO;
        this.deliveryTimeMinutes = deliveryTimeMinutes != null ? deliveryTimeMinutes : 30;
        this.isActive = isActive != null ? isActive : true;
        this.description = description;
        this.store = store;
    }

    // 편의 메소드
    public void updateAreaInfo(String areaName, String detailAddress, String description) {
        this.areaName = areaName;
        this.detailAddress = detailAddress;
        this.description = description;
    }

    public void updateDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void updateMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public void updateDeliveryTime(Integer deliveryTimeMinutes) {
        this.deliveryTimeMinutes = deliveryTimeMinutes;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // 주문 금액이 최소 주문 금액을 만족하는지 확인
    public boolean isOrderAmountValid(BigDecimal orderAmount) {
        return orderAmount.compareTo(this.minimumOrderAmount) >= 0;
    }

    // 총 결제 금액 계산 (주문 금액 + 배달비)
    public BigDecimal calculateTotalAmount(BigDecimal orderAmount) {
        return orderAmount.add(this.deliveryFee);
    }
} 