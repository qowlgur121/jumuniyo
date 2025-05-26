package com.jumuniyo.domain.store;

import com.jumuniyo.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 가게 이름

    @Column(length = 500)
    private String description; // 가게 설명

    @Column(nullable = false, length = 255)
    private String address; // 가게 주소

    @Column(length = 20)
    private String phoneNumber; // 가게 전화번호

    @Column(nullable = false, unique = true, length = 12)
    private String businessNumber; // 사업자 등록번호

    @Column(length = 255)
    private String logoImageUrl; // 가게 로고 이미지 URL

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO; // 평점 (0.00 ~ 5.00)

    @Column(nullable = false)
    private Integer reviewCount = 0; // 리뷰 개수

    @Column(nullable = false)
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO; // 최소 주문 금액

    @Column(nullable = false)
    private BigDecimal deliveryFee = BigDecimal.ZERO; // 배달비

    @Column(nullable = false)
    private Boolean isActive = true; // 영업 상태

    @Column(nullable = false)
    private Boolean isApproved = false; // 승인 상태

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // 카테고리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // 사장님 (User 엔티티)

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperatingHour> operatingHours = new ArrayList<>(); // 운영 시간

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryArea> deliveryAreas = new ArrayList<>(); // 배달 지역

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Store(String name, String description, String address, String phoneNumber, 
                 String businessNumber, String logoImageUrl, BigDecimal minimumOrderAmount, 
                 BigDecimal deliveryFee, Category category, User owner) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.businessNumber = businessNumber;
        this.logoImageUrl = logoImageUrl;
        this.minimumOrderAmount = minimumOrderAmount != null ? minimumOrderAmount : BigDecimal.ZERO;
        this.deliveryFee = deliveryFee != null ? deliveryFee : BigDecimal.ZERO;
        this.category = category;
        this.owner = owner;
    }

    // 편의 메소드
    public void updateBasicInfo(String name, String description, String address, String phoneNumber) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public void updateCategory(Category category) {
        this.category = category;
    }

    public void updateLogoImage(String logoImageUrl) {
        this.logoImageUrl = logoImageUrl;
    }

    public void updateDeliveryInfo(BigDecimal minimumOrderAmount, BigDecimal deliveryFee) {
        this.minimumOrderAmount = minimumOrderAmount;
        this.deliveryFee = deliveryFee;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void approve() {
        this.isApproved = true;
    }

    public void reject() {
        this.isApproved = false;
    }

    public void updateRating(BigDecimal rating, Integer reviewCount) {
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    // 연관관계 편의 메소드
    public void addOperatingHour(OperatingHour operatingHour) {
        this.operatingHours.add(operatingHour);
        operatingHour.setStore(this);
    }

    public void addDeliveryArea(DeliveryArea deliveryArea) {
        this.deliveryAreas.add(deliveryArea);
        deliveryArea.setStore(this);
    }

    // 소유자 확인
    public boolean isOwnedBy(User user) {
        return this.owner.getId().equals(user.getId());
    }

    public boolean isOwnedBy(Long ownerId) {
        return this.owner.getId().equals(ownerId);
    }
} 