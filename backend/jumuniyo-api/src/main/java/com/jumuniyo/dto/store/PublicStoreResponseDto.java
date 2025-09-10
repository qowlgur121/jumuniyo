package com.jumuniyo.dto.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 매장 공개 정보 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "매장 공개 정보")
public class PublicStoreResponseDto {

    @Schema(description = "매장 ID", example = "1")
    private Long id;

    @Schema(description = "매장명", example = "홍콩반점")
    private String name;

    @Schema(description = "매장 설명", example = "정통 중화요리 전문점")
    private String description;

    @Schema(description = "매장 주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "전화번호", example = "02-1234-5678")
    private String phoneNumber;

    @Schema(description = "카테고리", example = "중식")
    private String category;

    @Schema(description = "로고 이미지 URL")
    private String logoImageUrl;

    @Schema(description = "배너 이미지 URL")
    private String bannerImageUrl;

    @Schema(description = "평점", example = "4.5")
    private BigDecimal rating;

    @Schema(description = "리뷰 수", example = "127")
    private Integer reviewCount;

    @Schema(description = "최소 주문 금액", example = "15000")
    private BigDecimal minimumOrderAmount;

    @Schema(description = "기본 배달비", example = "3000")
    private BigDecimal deliveryFee;

    @Schema(description = "영업 시작 시간", example = "10:00")
    private LocalTime openTime;

    @Schema(description = "영업 종료 시간", example = "22:00")
    private LocalTime closeTime;

    @Schema(description = "영업 여부", example = "true")
    private Boolean isOpen;

    @Schema(description = "현재 운영 중 여부", example = "true")
    private Boolean isOperating;

    @Schema(description = "거리 (미터)", example = "1500")
    private Double distance;
} 