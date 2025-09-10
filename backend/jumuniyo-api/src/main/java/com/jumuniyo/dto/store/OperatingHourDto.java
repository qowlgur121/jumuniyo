package com.jumuniyo.dto.store;

import com.jumuniyo.domain.store.OperatingHour;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class OperatingHourDto {

    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isOpen;
    private Boolean isBreakTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;

    @Builder
    public OperatingHourDto(Long id, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime, 
                           Boolean isOpen, Boolean isBreakTime, LocalTime breakStartTime, 
                           LocalTime breakEndTime) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.isBreakTime = isBreakTime;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }

    // Entity를 DTO로 변환하는 정적 팩토리 메소드
    public static OperatingHourDto from(OperatingHour operatingHour) {
        return OperatingHourDto.builder()
                .id(operatingHour.getId())
                .dayOfWeek(operatingHour.getDayOfWeek())
                .openTime(operatingHour.getOpenTime())
                .closeTime(operatingHour.getCloseTime())
                .isOpen(operatingHour.getIsOpen())
                .isBreakTime(operatingHour.getIsBreakTime())
                .breakStartTime(operatingHour.getBreakStartTime())
                .breakEndTime(operatingHour.getBreakEndTime())
                .build();
    }
} 