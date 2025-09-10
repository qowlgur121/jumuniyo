package com.jumuniyo.dto.store;

import com.jumuniyo.domain.store.Category;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class StoreCreateRequestDto {

    @NotBlank(message = "가게 이름은 필수 입력 값입니다.")
    @Size(max = 100, message = "가게 이름은 최대 100자까지 입력 가능합니다.")
    private String name;

    @Size(max = 500, message = "가게 설명은 최대 500자까지 입력 가능합니다.")
    private String description;

    @NotBlank(message = "주소는 필수 입력 값입니다.")
    @Size(max = 255, message = "주소는 최대 255자까지 입력 가능합니다.")
    private String address;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호는 올바른 형식으로 입력해주세요. (예: 02-1234-5678)")
    private String phoneNumber;

    @NotBlank(message = "사업자 등록번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록번호는 올바른 형식으로 입력해주세요. (예: 123-45-67890)")
    private String businessNumber;

    @Size(max = 255, message = "로고 이미지 URL은 최대 255자까지 입력 가능합니다.")
    private String logoImageUrl;

    @NotNull(message = "최소 주문 금액은 필수 입력 값입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    @Digits(integer = 8, fraction = 2, message = "최소 주문 금액은 올바른 형식이어야 합니다.")
    private BigDecimal minimumOrderAmount;

    @NotNull(message = "배달비는 필수 입력 값입니다.")
    @DecimalMin(value = "0.0", inclusive = true, message = "배달비는 0원 이상이어야 합니다.")
    @Digits(integer = 8, fraction = 2, message = "배달비는 올바른 형식이어야 합니다.")
    private BigDecimal deliveryFee;

    @NotNull(message = "카테고리는 필수 선택 값입니다.")
    private Long categoryId;

    @Builder
    public StoreCreateRequestDto(String name, String description, String address, String phoneNumber, 
                                String businessNumber, String logoImageUrl, BigDecimal minimumOrderAmount, 
                                BigDecimal deliveryFee, Long categoryId) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.businessNumber = businessNumber;
        this.logoImageUrl = logoImageUrl;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.categoryId = categoryId;
    }

    // DTO를 Entity로 변환하는 메소드
    public Store toEntity(Category category, User owner) {
        return Store.builder()
                .name(this.name)
                .description(this.description)
                .address(this.address)
                .phoneNumber(this.phoneNumber)
                .businessNumber(this.businessNumber)
                .logoImageUrl(this.logoImageUrl)
                .minimumOrderAmount(this.minimumOrderAmount)
                .deliveryFee(this.deliveryFee)
                .category(category)
                .owner(owner)
                .build();
    }
} 