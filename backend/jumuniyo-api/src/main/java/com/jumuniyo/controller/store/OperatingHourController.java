package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.OperatingHourCreateRequestDto;
import com.jumuniyo.dto.store.OperatingHourDto;
import com.jumuniyo.dto.store.OperatingHourUpdateRequestDto;
import com.jumuniyo.service.store.OperatingHourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.util.List;

/**
 * 운영시간 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class OperatingHourController {

    private final OperatingHourService operatingHourService;

    /**
     * 특정 음식점의 운영시간 생성
     */
    @PostMapping("/{storeId}/operating-hours")
    public ResponseEntity<OperatingHourDto> createOperatingHour(
            @PathVariable Long storeId,
            @Valid @RequestBody OperatingHourCreateRequestDto requestDto) {
        
        log.info("운영시간 생성 요청 - 음식점 ID: {}, 요일: {}", storeId, requestDto.getDayOfWeek());
        OperatingHourDto operatingHour = operatingHourService.createOperatingHour(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(operatingHour);
    }

    /**
     * 특정 음식점의 운영시간 일괄 생성/수정
     */
    @PostMapping("/{storeId}/operating-hours/batch")
    public ResponseEntity<List<OperatingHourDto>> createOrUpdateOperatingHours(
            @PathVariable Long storeId,
            @Valid @RequestBody List<OperatingHourCreateRequestDto> requestDtos) {
        
        log.info("운영시간 일괄 저장 요청 - 음식점 ID: {}, 요일 수: {}", storeId, requestDtos.size());
        List<OperatingHourDto> operatingHours = operatingHourService.createOrUpdateOperatingHours(storeId, requestDtos);
        return ResponseEntity.ok(operatingHours);
    }

    /**
     * 특정 음식점의 모든 운영시간 조회
     */
    @GetMapping("/{storeId}/operating-hours")
    public ResponseEntity<List<OperatingHourDto>> getOperatingHoursByStoreId(@PathVariable Long storeId) {
        log.info("운영시간 목록 조회 요청 - 음식점 ID: {}", storeId);
        List<OperatingHourDto> operatingHours = operatingHourService.getOperatingHoursByStoreId(storeId);
        return ResponseEntity.ok(operatingHours);
    }

    /**
     * 특정 음식점의 특정 요일 운영시간 조회
     */
    @GetMapping("/{storeId}/operating-hours/{dayOfWeek}")
    public ResponseEntity<OperatingHourDto> getOperatingHourByStoreAndDay(
            @PathVariable Long storeId,
            @PathVariable DayOfWeek dayOfWeek) {
        
        log.info("특정 요일 운영시간 조회 요청 - 음식점 ID: {}, 요일: {}", storeId, dayOfWeek);
        OperatingHourDto operatingHour = operatingHourService.getOperatingHourByStoreAndDay(storeId, dayOfWeek);
        return ResponseEntity.ok(operatingHour);
    }

    /**
     * 운영시간 수정 (운영시간 ID로)
     */
    @PutMapping("/{storeId}/operating-hours/{operatingHourId}")
    public ResponseEntity<OperatingHourDto> updateOperatingHour(
            @PathVariable Long storeId,
            @PathVariable Long operatingHourId,
            @Valid @RequestBody OperatingHourUpdateRequestDto requestDto) {
        
        log.info("운영시간 수정 요청 - 음식점 ID: {}, 운영시간 ID: {}", storeId, operatingHourId);
        OperatingHourDto operatingHour = operatingHourService.updateOperatingHour(operatingHourId, requestDto);
        return ResponseEntity.ok(operatingHour);
    }

    /**
     * 특정 음식점의 특정 요일 운영시간 수정
     */
    @PutMapping("/{storeId}/operating-hours/day/{dayOfWeek}")
    public ResponseEntity<OperatingHourDto> updateOperatingHourByStoreAndDay(
            @PathVariable Long storeId,
            @PathVariable DayOfWeek dayOfWeek,
            @Valid @RequestBody OperatingHourUpdateRequestDto requestDto) {
        
        log.info("특정 요일 운영시간 수정 요청 - 음식점 ID: {}, 요일: {}", storeId, dayOfWeek);
        OperatingHourDto operatingHour = operatingHourService.updateOperatingHourByStoreAndDay(storeId, dayOfWeek, requestDto);
        return ResponseEntity.ok(operatingHour);
    }

    /**
     * 운영시간 삭제
     */
    @DeleteMapping("/{storeId}/operating-hours/{operatingHourId}")
    public ResponseEntity<Void> deleteOperatingHour(
            @PathVariable Long storeId,
            @PathVariable Long operatingHourId) {
        
        log.info("운영시간 삭제 요청 - 음식점 ID: {}, 운영시간 ID: {}", storeId, operatingHourId);
        operatingHourService.deleteOperatingHour(operatingHourId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 음식점의 모든 운영시간 삭제
     */
    @DeleteMapping("/{storeId}/operating-hours")
    public ResponseEntity<Void> deleteAllOperatingHoursByStoreId(@PathVariable Long storeId) {
        log.info("음식점의 모든 운영시간 삭제 요청 - 음식점 ID: {}", storeId);
        operatingHourService.deleteAllOperatingHoursByStoreId(storeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 음식점의 영업 중인 요일들만 조회
     */
    @GetMapping("/{storeId}/operating-hours/open-days")
    public ResponseEntity<List<OperatingHourDto>> getOpenDaysByStoreId(@PathVariable Long storeId) {
        log.info("영업 중인 요일 조회 요청 - 음식점 ID: {}", storeId);
        List<OperatingHourDto> openDays = operatingHourService.getOpenDaysByStoreId(storeId);
        return ResponseEntity.ok(openDays);
    }

    /**
     * 현재 시간에 영업 중인지 확인
     */
    @GetMapping("/{storeId}/operating-hours/is-open-now")
    public ResponseEntity<Boolean> isStoreOpenNow(@PathVariable Long storeId) {
        log.info("현재 영업 상태 확인 요청 - 음식점 ID: {}", storeId);
        boolean isOpen = operatingHourService.isStoreOpenNow(storeId);
        return ResponseEntity.ok(isOpen);
    }

    /**
     * 특정 요일에 영업 중인 음식점들의 운영시간 조회
     */
    @GetMapping("/operating-hours/open-stores/{dayOfWeek}")
    public ResponseEntity<List<OperatingHourDto>> getCurrentlyOpenStores(@PathVariable DayOfWeek dayOfWeek) {
        log.info("특정 요일 영업 중인 음식점 조회 요청 - 요일: {}", dayOfWeek);
        List<OperatingHourDto> openStores = operatingHourService.getCurrentlyOpenStores(dayOfWeek);
        return ResponseEntity.ok(openStores);
    }
} 