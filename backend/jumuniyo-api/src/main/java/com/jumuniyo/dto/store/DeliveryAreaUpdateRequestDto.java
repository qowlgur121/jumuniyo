package com.jumuniyo.dto.store;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class DeliveryAreaUpdateRequestDto {

    @Size(max = 100, message = "배달 지역명은 100자 이하로 입력해주세요.")
    private String areaName;

    @Size(max = 255, message = "상세 주소는 255자 이하로 입력해주세요.")
    private String detailAddress;

    @DecimalMin(value = "0", message = "배달비는 0원 이상이어야 합니다.")
    @Digits(integer = 8, fraction = 2, message = "배달비는 올바른 금액 형식으로 입력해주세요.")
    private BigDecimal deliveryFee;

    @DecimalMin(value = "0", message = "최소 주문 금액은 0원 이상이어야 합니다.")
    @Digits(integer = 8, fraction = 2, message = "최소 주문 금액은 올바른 금액 형식으로 입력해주세요.")
    private BigDecimal minimumOrderAmount;

    @Min(value = 1, message = "배달 예상 시간은 1분 이상이어야 합니다.")
    @Max(value = 180, message = "배달 예상 시간은 180분 이하여야 합니다.")
    private Integer deliveryTimeMinutes;

    private Boolean isActive;

    @Size(max = 500, message = "설명은 500자 이하로 입력해주세요.")
    private String description;

    @Builder
    public DeliveryAreaUpdateRequestDto(String areaName, String detailAddress, BigDecimal deliveryFee,
                                       BigDecimal minimumOrderAmount, Integer deliveryTimeMinutes,
                                       Boolean isActive, String description) {
        this.areaName = areaName;
        this.detailAddress = detailAddress;
        this.deliveryFee = deliveryFee;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryTimeMinutes = deliveryTimeMinutes;
        this.isActive = isActive;
        this.description = description;
    }
} 