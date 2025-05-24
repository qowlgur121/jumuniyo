package com.jumuniyo.dto.store;

import com.jumuniyo.domain.store.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class StoreResponseDto {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private String businessNumber;
    private String logoImageUrl;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal minimumOrderAmount;
    private BigDecimal deliveryFee;
    private Boolean isActive;
    private Boolean isApproved;
    private CategoryResponseDto category;
    private Long ownerId;
    private String ownerEmail;
    private List<OperatingHourDto> operatingHours;
    private List<DeliveryAreaDto> deliveryAreas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public StoreResponseDto(Long id, String name, String description, String address, 
                           String phoneNumber, String businessNumber, String logoImageUrl, 
                           BigDecimal rating, Integer reviewCount, BigDecimal minimumOrderAmount, 
                           BigDecimal deliveryFee, Boolean isActive, Boolean isApproved, 
                           CategoryResponseDto category, Long ownerId, String ownerEmail, 
                           List<OperatingHourDto> operatingHours, List<DeliveryAreaDto> deliveryAreas,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.businessNumber = businessNumber;
        this.logoImageUrl = logoImageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
        this.isActive = isActive;
        this.isApproved = isApproved;
        this.category = category;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.operatingHours = operatingHours;
        this.deliveryAreas = deliveryAreas;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Entity를 DTO로 변환하는 정적 팩토리 메소드
    public static StoreResponseDto from(Store store) {
        return StoreResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phoneNumber(store.getPhoneNumber())
                .businessNumber(store.getBusinessNumber())
                .logoImageUrl(store.getLogoImageUrl())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .minimumOrderAmount(store.getMinimumOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .isActive(store.getIsActive())
                .isApproved(store.getIsApproved())
                .category(CategoryResponseDto.from(store.getCategory()))
                .ownerId(store.getOwner().getId())
                .ownerEmail(store.getOwner().getEmail())
                .operatingHours(store.getOperatingHours().stream()
                        .map(OperatingHourDto::from)
                        .collect(Collectors.toList()))
                .deliveryAreas(store.getDeliveryAreas().stream()
                        .map(DeliveryAreaDto::from)
                        .collect(Collectors.toList()))
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    // 간소화된 버전 (목록용)
    public static StoreResponseDto fromSimple(Store store) {
        return StoreResponseDto.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .logoImageUrl(store.getLogoImageUrl())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .minimumOrderAmount(store.getMinimumOrderAmount())
                .deliveryFee(store.getDeliveryFee())
                .isActive(store.getIsActive())
                .isApproved(store.getIsApproved())
                .category(CategoryResponseDto.from(store.getCategory()))
                .createdAt(store.getCreatedAt())
                .build();
    }
} 