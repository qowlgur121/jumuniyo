package com.jumuniyo.dto.store;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class OperatingHourUpdateRequestDto {

    private LocalTime openTime;

    private LocalTime closeTime;

    private Boolean isOpen;

    private Boolean isBreakTime;

    private LocalTime breakStartTime;

    private LocalTime breakEndTime;

    @Builder
    public OperatingHourUpdateRequestDto(LocalTime openTime, LocalTime closeTime, Boolean isOpen,
                                        Boolean isBreakTime, LocalTime breakStartTime, LocalTime breakEndTime) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
        this.isBreakTime = isBreakTime;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }
} 