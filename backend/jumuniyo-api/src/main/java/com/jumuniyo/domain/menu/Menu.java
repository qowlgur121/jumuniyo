package com.jumuniyo.domain.menu;

import com.jumuniyo.domain.store.Store;
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
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 메뉴 이름

    @Column(length = 500)
    private String description; // 메뉴 설명

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // 기본 가격

    @Column(length = 255)
    private String imageUrl; // 메뉴 이미지 URL

    @Column(nullable = false)
    private Boolean isAvailable = true; // 판매 가능 여부

    @Column(nullable = false)
    private Boolean isRecommended = false; // 추천 메뉴 여부

    @Column(nullable = false)
    private Integer displayOrder = 0; // 표시 순서

    @Column(nullable = false)
    private Integer soldCount = 0; // 판매 수량

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 소속 음식점

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id", nullable = false)
    private MenuCategory category; // 메뉴 카테고리

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuOptionGroup> optionGroups = new ArrayList<>(); // 메뉴 옵션 그룹들

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Menu(String name, String description, BigDecimal price, String imageUrl, 
                Boolean isRecommended, Integer displayOrder, Store store, MenuCategory category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isRecommended = isRecommended != null ? isRecommended : false;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.store = store;
        this.category = category;
    }

    // 편의 메소드
    public void updateBasicInfo(String name, String description, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public void updateCategory(MenuCategory category) {
        this.category = category;
    }

    public void makeAvailable() {
        this.isAvailable = true;
    }

    public void makeUnavailable() {
        this.isAvailable = false;
    }

    public void setRecommended(Boolean isRecommended) {
        this.isRecommended = isRecommended;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void incrementSoldCount() {
        this.soldCount++;
    }

    public void updateSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    // 연관관계 편의 메소드
    public void addOptionGroup(MenuOptionGroup optionGroup) {
        this.optionGroups.add(optionGroup);
        optionGroup.setMenu(this);
    }

    public void removeOptionGroup(MenuOptionGroup optionGroup) {
        this.optionGroups.remove(optionGroup);
        optionGroup.setMenu(null);
    }

    // setter for category (JPA 연관관계 설정용)
    public void setCategory(MenuCategory category) {
        this.category = category;
    }
} 