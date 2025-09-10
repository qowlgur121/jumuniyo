package com.jumuniyo.domain.menu;

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
@Table(name = "menu_option_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MenuOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_option_group_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 옵션 그룹 이름 (예: 맵기 선택, 토핑 추가)

    @Column(length = 200)
    private String description; // 옵션 그룹 설명

    @Column(nullable = false)
    private Boolean isRequired = false; // 필수 선택 여부

    @Column(nullable = false)
    private Integer minSelection = 0; // 최소 선택 개수

    @Column(nullable = false)
    private Integer maxSelection = 1; // 최대 선택 개수

    @Column(nullable = false)
    private Integer displayOrder = 0; // 표시 순서

    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 상태

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu; // 소속 메뉴

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuOption> options = new ArrayList<>(); // 옵션들

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MenuOptionGroup(String name, String description, Boolean isRequired, 
                          Integer minSelection, Integer maxSelection, Integer displayOrder, Menu menu) {
        this.name = name;
        this.description = description;
        this.isRequired = isRequired != null ? isRequired : false;
        this.minSelection = minSelection != null ? minSelection : 0;
        this.maxSelection = maxSelection != null ? maxSelection : 1;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.menu = menu;
    }

    // 편의 메소드
    public void updateInfo(String name, String description, Boolean isRequired, 
                          Integer minSelection, Integer maxSelection, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.isRequired = isRequired;
        this.minSelection = minSelection;
        this.maxSelection = maxSelection;
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
    public void addOption(MenuOption option) {
        this.options.add(option);
        option.setOptionGroup(this);
    }

    public void removeOption(MenuOption option) {
        this.options.remove(option);
        option.setOptionGroup(null);
    }

    // setter for menu (JPA 연관관계 설정용)
    public void setMenu(Menu menu) {
        this.menu = menu;
    }
} 