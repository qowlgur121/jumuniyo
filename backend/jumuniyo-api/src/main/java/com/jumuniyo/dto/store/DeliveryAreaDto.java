package com.jumuniyo.dto.store;

import com.jumuniyo.domain.store.DeliveryArea;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class DeliveryAreaDto {

    private Long id;
    private String areaName;
    private String detailAddress;
    private BigDecimal deliveryFee;
    private BigDecimal minimumOrderAmount;
    private Integer deliveryTimeMinutes;
    private Boolean isActive;
    private String description;

    @Builder
    public DeliveryAreaDto(Long id, String areaName, String detailAddress, BigDecimal deliveryFee, 
                          BigDecimal minimumOrderAmount, Integer deliveryTimeMinutes, Boolean isActive, 
                          String description) {
        this.id = id;
        this.areaName = areaName;
        this.detailAddress = detailAddress;
        this.deliveryFee = deliveryFee;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryTimeMinutes = deliveryTimeMinutes;
        this.isActive = isActive;
        this.description = description;
    }

    // Entity를 DTO로 변환하는 정적 팩토리 메소드
    public static DeliveryAreaDto from(DeliveryArea deliveryArea) {
        return DeliveryAreaDto.builder()
                .id(deliveryArea.getId())
                .areaName(deliveryArea.getAreaName())
                .detailAddress(deliveryArea.getDetailAddress())
                .deliveryFee(deliveryArea.getDeliveryFee())
                .minimumOrderAmount(deliveryArea.getMinimumOrderAmount())
                .deliveryTimeMinutes(deliveryArea.getDeliveryTimeMinutes())
                .isActive(deliveryArea.getIsActive())
                .description(deliveryArea.getDescription())
                .build();
    }
} 