package com.jumuniyo.domain.menu;

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

@Entity
@Table(name = "menu_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_option_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 옵션 이름 (예: 순한맛, 매운맛, 치즈 추가)

    @Column(length = 200)
    private String description; // 옵션 설명

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPrice = BigDecimal.ZERO; // 추가 가격

    @Column(nullable = false)
    private Integer displayOrder = 0; // 표시 순서

    @Column(nullable = false)
    private Boolean isAvailable = true; // 선택 가능 여부

    @Column(nullable = false)
    private Boolean isDefault = false; // 기본 선택 여부

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_group_id", nullable = false)
    private MenuOptionGroup optionGroup; // 소속 옵션 그룹

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MenuOption(String name, String description, BigDecimal additionalPrice, 
                     Integer displayOrder, Boolean isDefault, MenuOptionGroup optionGroup) {
        this.name = name;
        this.description = description;
        this.additionalPrice = additionalPrice != null ? additionalPrice : BigDecimal.ZERO;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isDefault = isDefault != null ? isDefault : false;
        this.optionGroup = optionGroup;
    }

    // 편의 메소드
    public void updateInfo(String name, String description, BigDecimal additionalPrice, 
                          Integer displayOrder, Boolean isDefault) {
        this.name = name;
        this.description = description;
        this.additionalPrice = additionalPrice;
        this.displayOrder = displayOrder;
        this.isDefault = isDefault;
    }

    public void makeAvailable() {
        this.isAvailable = true;
    }

    public void makeUnavailable() {
        this.isAvailable = false;
    }

    public void setAsDefault() {
        this.isDefault = true;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void updateAdditionalPrice(BigDecimal additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    // setter for optionGroup (JPA 연관관계 설정용)
    public void setOptionGroup(MenuOptionGroup optionGroup) {
        this.optionGroup = optionGroup;
    }
} 