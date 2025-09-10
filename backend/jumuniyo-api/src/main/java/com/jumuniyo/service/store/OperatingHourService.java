package com.jumuniyo.service.store;

import com.jumuniyo.dto.store.OperatingHourCreateRequestDto;
import com.jumuniyo.dto.store.OperatingHourDto;
import com.jumuniyo.dto.store.OperatingHourUpdateRequestDto;

import java.time.DayOfWeek;
import java.util.List;

/**
 * 운영시간 관리 서비스 인터페이스
 */
public interface OperatingHourService {

    /**
     * 특정 음식점의 운영시간 생성
     */
    OperatingHourDto createOperatingHour(Long storeId, OperatingHourCreateRequestDto requestDto);

    /**
     * 특정 음식점의 운영시간 일괄 생성/수정
     */
    List<OperatingHourDto> createOrUpdateOperatingHours(Long storeId, List<OperatingHourCreateRequestDto> requestDtos);

    /**
     * 특정 음식점의 모든 운영시간 조회
     */
    List<OperatingHourDto> getOperatingHoursByStoreId(Long storeId);

    /**
     * 특정 음식점의 특정 요일 운영시간 조회
     */
    OperatingHourDto getOperatingHourByStoreAndDay(Long storeId, DayOfWeek dayOfWeek);

    /**
     * 운영시간 수정
     */
    OperatingHourDto updateOperatingHour(Long operatingHourId, OperatingHourUpdateRequestDto requestDto);

    /**
     * 특정 음식점의 특정 요일 운영시간 수정
     */
    OperatingHourDto updateOperatingHourByStoreAndDay(Long storeId, DayOfWeek dayOfWeek, OperatingHourUpdateRequestDto requestDto);

    /**
     * 운영시간 삭제
     */
    void deleteOperatingHour(Long operatingHourId);

    /**
     * 특정 음식점의 모든 운영시간 삭제
     */
    void deleteAllOperatingHoursByStoreId(Long storeId);

    /**
     * 특정 음식점의 영업 중인 요일들만 조회
     */
    List<OperatingHourDto> getOpenDaysByStoreId(Long storeId);

    /**
     * 현재 시간에 영업 중인지 확인
     */
    boolean isStoreOpenNow(Long storeId);

    /**
     * 특정 요일에 영업 중인 음식점들의 운영시간 조회
     */
    List<OperatingHourDto> getCurrentlyOpenStores(DayOfWeek dayOfWeek);
} 