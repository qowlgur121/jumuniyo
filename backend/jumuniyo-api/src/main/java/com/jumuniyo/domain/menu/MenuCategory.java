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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_category_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 카테고리 이름 (예: 메인메뉴, 사이드메뉴, 음료)

    @Column(length = 200)
    private String description; // 카테고리 설명

    @Column(nullable = false)
    private Integer displayOrder = 0; // 표시 순서

    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 상태

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 소속 음식점

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus = new ArrayList<>(); // 해당 카테고리의 메뉴들

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MenuCategory(String name, String description, Integer displayOrder, Store store) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.store = store;
    }

    // 편의 메소드
    public void updateInfo(String name, String description, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // 연관관계 편의 메소드
    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.setCategory(this);
    }

    public void removeMenu(Menu menu) {
        this.menus.remove(menu);
        menu.setCategory(null);
    }
} 