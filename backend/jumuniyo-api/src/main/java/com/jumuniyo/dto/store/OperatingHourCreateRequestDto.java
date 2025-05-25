package com.jumuniyo.dto.store;

import com.jumuniyo.domain.store.OperatingHour;
import com.jumuniyo.domain.store.Store;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class OperatingHourCreateRequestDto {

    @NotNull(message = "요일은 필수 입력 값입니다.")
    private DayOfWeek dayOfWeek;

    private LocalTime openTime;

    private LocalTime closeTime;

    @NotNull(message = "영업 여부는 필수 입력 값입니다.")
    private Boolean isOpen;

    private Boolean isBreakTime;

    private LocalTime breakStartTime;

    private LocalTime breakEndTime;

    @Builder
    public OperatingHourCreateRequestDto(DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime,
                                        Boolean isOpen, Boolean isBreakTime, LocalTime breakStartTime,
                                        LocalTime breakEndTime) {
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.isBreakTime = isBreakTime;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }

    // DTO를 Entity로 변환하는 메소드
    public OperatingHour toEntity(Store store) {
        return OperatingHour.builder()
                .store(store)
                .dayOfWeek(this.dayOfWeek)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .isOpen(this.isOpen != null ? this.isOpen : false)
                .isBreakTime(this.isBreakTime != null ? this.isBreakTime : false)
                .breakStartTime(this.breakStartTime)
                .breakEndTime(this.breakEndTime)
                .build();
    }
} 