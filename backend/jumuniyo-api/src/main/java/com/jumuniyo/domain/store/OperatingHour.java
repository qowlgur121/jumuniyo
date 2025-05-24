package com.jumuniyo.domain.store;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "operating_hours")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OperatingHour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operating_hour_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DayOfWeek dayOfWeek; // 요일 (MONDAY, TUESDAY, ...)

    @Column(nullable = false)
    private LocalTime openTime; // 오픈 시간

    @Column(nullable = false)
    private LocalTime closeTime; // 마감 시간

    @Column(nullable = false)
    private Boolean isOpen = true; // 해당 요일 영업 여부

    @Column(nullable = false)
    private Boolean isBreakTime = false; // 브레이크 타임 여부

    private LocalTime breakStartTime; // 브레이크 시작 시간

    private LocalTime breakEndTime; // 브레이크 종료 시간

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @Setter // Store 엔티티에서 설정할 수 있도록 Setter 추가
    private Store store;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public OperatingHour(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, 
                        Boolean isOpen, Boolean isBreakTime, LocalTime breakStartTime, 
                        LocalTime breakEndTime, Store store) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen != null ? isOpen : true;
        this.isBreakTime = isBreakTime != null ? isBreakTime : false;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
        this.store = store;
    }

    // 편의 메소드
    public void updateOperatingTime(LocalTime openTime, LocalTime closeTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public void updateBreakTime(LocalTime breakStartTime, LocalTime breakEndTime) {
        this.isBreakTime = true;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }

    public void removeBreakTime() {
        this.isBreakTime = false;
        this.breakStartTime = null;
        this.breakEndTime = null;
    }

    public void open() {
        this.isOpen = true;
    }

    public void close() {
        this.isOpen = false;
    }

    // 현재 시간이 영업 시간인지 확인
    public boolean isOpenAt(LocalTime time) {
        if (!isOpen) {
            return false;
        }

        boolean isInOperatingHours = time.isAfter(openTime) && time.isBefore(closeTime);
        
        if (isInOperatingHours && isBreakTime) {
            // 브레이크 타임인지 확인
            boolean isInBreakTime = time.isAfter(breakStartTime) && time.isBefore(breakEndTime);
            return !isInBreakTime;
        }

        return isInOperatingHours;
    }
} 