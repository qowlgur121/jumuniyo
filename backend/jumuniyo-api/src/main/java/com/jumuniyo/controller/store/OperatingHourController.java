package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.OperatingHourCreateRequestDto;
import com.jumuniyo.dto.store.OperatingHourDto;
import com.jumuniyo.dto.store.OperatingHourUpdateRequestDto;
import com.jumuniyo.service.store.OperatingHourService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 운영시간 관리 REST API Controller
 */
@Tag(name = "운영시간 관리 API (사업자용)", description = "매장의 운영시간 생성, 수정, 조회, 삭제 및 영업 상태 확인 API (사업자 인증 필요 - X-Owner-Id 헤더 사용)")
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class OperatingHourController {

    private final OperatingHourService operatingHourService;

    @Operation(
        summary = "운영시간 생성",
        description = "특정 매장의 새로운 운영시간을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "운영시간 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/operating-hours")
    public ResponseEntity<OperatingHourDto> createOperatingHour(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody OperatingHourCreateRequestDto requestDto) {
        
        log.info("운영시간 생성 요청 - 음식점 ID: {}, 요일: {}", storeId, requestDto.getDayOfWeek());
        OperatingHourDto operatingHour = operatingHourService.createOperatingHour(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(operatingHour);
    }

    @Operation(
        summary = "운영시간 일괄 생성/수정",
        description = "특정 매장의 운영시간들을 일괄로 생성하거나 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운영시간 일괄 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/operating-hours/batch")
    public ResponseEntity<List<OperatingHourDto>> createOrUpdateOperatingHours(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody List<OperatingHourCreateRequestDto> requestDtos) {
        
        log.info("운영시간 일괄 저장 요청 - 음식점 ID: {}, 요일 수: {}", storeId, requestDtos.size());
        List<OperatingHourDto> operatingHours = operatingHourService.createOrUpdateOperatingHours(storeId, requestDtos);
        return ResponseEntity.ok(operatingHours);
    }

    @Operation(
        summary = "매장 운영시간 목록 조회",
        description = "특정 매장의 모든 운영시간 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운영시간 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours")
    public ResponseEntity<List<OperatingHourDto>> getOperatingHoursByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("운영시간 목록 조회 요청 - 음식점 ID: {}", storeId);
        List<OperatingHourDto> operatingHours = operatingHourService.getOperatingHoursByStoreId(storeId);
        return ResponseEntity.ok(operatingHours);
    }

    @Operation(
        summary = "요일별 운영시간 조회",
        description = "특정 매장의 특정 요일 운영시간을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운영시간 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장 또는 운영시간을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/{dayOfWeek}")
    public ResponseEntity<OperatingHourDto> getOperatingHourByStoreAndDay(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "요일 (MONDAY~SUNDAY)", required = true, example = "MONDAY")
            @PathVariable DayOfWeek dayOfWeek) {
        
        log.info("특정 요일 운영시간 조회 요청 - 음식점 ID: {}, 요일: {}", storeId, dayOfWeek);
        OperatingHourDto operatingHour = operatingHourService.getOperatingHourByStoreAndDay(storeId, dayOfWeek);
        return ResponseEntity.ok(operatingHour);
    }

    @Operation(
        summary = "운영시간 수정",
        description = "운영시간 ID로 특정 운영시간의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운영시간 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "운영시간을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{storeId}/operating-hours/{operatingHourId}")
    public ResponseEntity<OperatingHourDto> updateOperatingHour(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "운영시간 ID", required = true, example = "1")
            @PathVariable Long operatingHourId,
            @Valid @RequestBody OperatingHourUpdateRequestDto requestDto) {
        
        log.info("운영시간 수정 요청 - 음식점 ID: {}, 운영시간 ID: {}", storeId, operatingHourId);
        OperatingHourDto operatingHour = operatingHourService.updateOperatingHour(operatingHourId, requestDto);
        return ResponseEntity.ok(operatingHour);
    }

    @Operation(
        summary = "요일별 운영시간 수정",
        description = "매장 ID와 요일로 특정 운영시간의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "운영시간 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "운영시간을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{storeId}/operating-hours/day/{dayOfWeek}")
    public ResponseEntity<OperatingHourDto> updateOperatingHourByStoreAndDay(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "요일 (MONDAY~SUNDAY)", required = true, example = "MONDAY")
            @PathVariable DayOfWeek dayOfWeek,
            @Valid @RequestBody OperatingHourUpdateRequestDto requestDto) {
        
        log.info("특정 요일 운영시간 수정 요청 - 음식점 ID: {}, 요일: {}", storeId, dayOfWeek);
        OperatingHourDto operatingHour = operatingHourService.updateOperatingHourByStoreAndDay(storeId, dayOfWeek, requestDto);
        return ResponseEntity.ok(operatingHour);
    }

    @Operation(
        summary = "운영시간 삭제",
        description = "특정 운영시간을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "운영시간 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "운영시간을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}/operating-hours/{operatingHourId}")
    public ResponseEntity<Void> deleteOperatingHour(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "운영시간 ID", required = true, example = "1")
            @PathVariable Long operatingHourId) {
        
        log.info("운영시간 삭제 요청 - 음식점 ID: {}, 운영시간 ID: {}", storeId, operatingHourId);
        operatingHourService.deleteOperatingHour(operatingHourId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "매장 운영시간 전체 삭제",
        description = "특정 매장의 모든 운영시간을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "운영시간 전체 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}/operating-hours")
    public ResponseEntity<Void> deleteAllOperatingHoursByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점의 모든 운영시간 삭제 요청 - 음식점 ID: {}", storeId);
        operatingHourService.deleteAllOperatingHoursByStoreId(storeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "영업 중인 요일 조회",
        description = "특정 매장의 영업 중인 요일들만 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 중인 요일 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/open-days")
    public ResponseEntity<List<OperatingHourDto>> getOpenDaysByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("영업 중인 요일 조회 요청 - 음식점 ID: {}", storeId);
        List<OperatingHourDto> openDays = operatingHourService.getOpenDaysByStoreId(storeId);
        return ResponseEntity.ok(openDays);
    }

    @Operation(
        summary = "현재 시간 영업 상태 확인",
        description = "현재 시간에 매장이 영업중인지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 상태 확인 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/is-open")
    public ResponseEntity<Boolean> isStoreOpenNow(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("현재 시간 영업 상태 확인 요청 - 음식점 ID: {}", storeId);
        boolean isOpen = operatingHourService.isStoreOpenNow(storeId);
        return ResponseEntity.ok(isOpen);
    }

    @Operation(
        summary = "특정 요일 영업 중인 매장 조회",
        description = "특정 요일에 영업 중인 매장들의 운영시간을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 중인 매장 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요일 정보"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/operating-hours/open-stores/{dayOfWeek}")
    public ResponseEntity<List<OperatingHourDto>> getCurrentlyOpenStores(
            @Parameter(description = "요일 (MONDAY~SUNDAY)", required = true, example = "MONDAY")
            @PathVariable DayOfWeek dayOfWeek) {
        log.info("특정 요일 영업 중인 음식점 조회 요청 - 요일: {}", dayOfWeek);
        List<OperatingHourDto> openStores = operatingHourService.getCurrentlyOpenStores(dayOfWeek);
        return ResponseEntity.ok(openStores);
    }

    @Operation(
        summary = "특정 시간 영업 상태 확인",
        description = "특정 시간에 매장이 영업중인지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 상태 확인 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/is-open-at")
    public ResponseEntity<Boolean> isStoreOpenAt(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "확인할 시간", required = true, example = "2024-01-15T14:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        
        log.info("특정 시간 영업 상태 확인 요청 - 음식점 ID: {}, 시간: {}", storeId, dateTime);
        // TODO: OperatingHourService.isStoreOpenAt 메서드 구현 후 활성화
        // boolean isOpen = operatingHourService.isStoreOpenAt(storeId, dateTime);
        // return ResponseEntity.ok(isOpen);
        
        // 임시 응답
        return ResponseEntity.ok(false);
    }

    @Operation(
        summary = "오늘 운영시간 조회",
        description = "오늘 요일의 매장 운영시간을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "오늘 운영시간 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/today")
    public ResponseEntity<OperatingHourDto> getTodayOperatingHour(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("오늘 운영시간 조회 요청 - 음식점 ID: {}", storeId);
        // TODO: OperatingHourService.getTodayOperatingHour 메서드 구현 후 활성화
        // OperatingHourDto todayOperatingHour = operatingHourService.getTodayOperatingHour(storeId);
        // return ResponseEntity.ok(todayOperatingHour);
        
        // 임시 응답
        return ResponseEntity.ok(new OperatingHourDto());
    }

    @Operation(
        summary = "24시간 운영 설정",
        description = "매장을 24시간 운영으로 설정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "24시간 운영 설정 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{storeId}/operating-hours/set-24-hours")
    public ResponseEntity<List<OperatingHourDto>> set24HoursOperation(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("24시간 운영 설정 요청 - 음식점 ID: {}", storeId);
        // TODO: OperatingHourService.set24HoursOperation 메서드 구현 후 활성화
        // List<OperatingHourDto> operatingHours = operatingHourService.set24HoursOperation(storeId);
        // return ResponseEntity.ok(operatingHours);
        
        // 임시 응답
        return ResponseEntity.ok(List.of());
    }

    @Operation(
        summary = "동일시간 일괄 설정",
        description = "모든 요일을 동일한 운영시간으로 설정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "동일시간 일괄 설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{storeId}/operating-hours/set-same-hours")
    public ResponseEntity<List<OperatingHourDto>> setSameHoursForAllDays(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "개점 시간", required = true, example = "09:00")
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime openTime,
            @Parameter(description = "마감 시간", required = true, example = "22:00")
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime closeTime,
            @Parameter(description = "브레이크 타임 시작", required = false, example = "15:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime breakStartTime,
            @Parameter(description = "브레이크 타임 종료", required = false, example = "17:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm") LocalTime breakEndTime) {
        
        log.info("동일시간 일괄 설정 요청 - 음식점 ID: {}, 개점: {}, 마감: {}", storeId, openTime, closeTime);
        // TODO: OperatingHourService.setSameHoursForAllDays 메서드 구현 후 활성화
        // List<OperatingHourDto> operatingHours = operatingHourService.setSameHoursForAllDays(storeId, openTime, closeTime, breakStartTime, breakEndTime);
        // return ResponseEntity.ok(operatingHours);
        
        // 임시 응답
        return ResponseEntity.ok(List.of());
    }

    @Operation(
        summary = "영업 상태 토글",
        description = "특정 요일의 영업 상태를 토글합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 상태 토글 성공"),
        @ApiResponse(responseCode = "404", description = "운영시간을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{storeId}/operating-hours/{operatingHourId}/toggle-open")
    public ResponseEntity<OperatingHourDto> toggleOperatingStatus(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "운영시간 ID", required = true, example = "1")
            @PathVariable Long operatingHourId) {
        
        log.info("영업 상태 토글 요청 - 음식점 ID: {}, 운영시간 ID: {}", storeId, operatingHourId);
        // TODO: OperatingHourService.toggleOperatingStatus 메서드 구현 후 활성화
        // OperatingHourDto operatingHour = operatingHourService.toggleOperatingStatus(operatingHourId);
        // return ResponseEntity.ok(operatingHour);
        
        // 임시 응답
        return ResponseEntity.ok(new OperatingHourDto());
    }

    @Operation(
        summary = "주간 운영 스케줄 조회",
        description = "한 주간의 전체 운영 스케줄을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주간 운영 스케줄 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/operating-hours/weekly-schedule")
    public ResponseEntity<Map<DayOfWeek, OperatingHourDto>> getWeeklySchedule(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("주간 운영 스케줄 조회 요청 - 음식점 ID: {}", storeId);
        // TODO: OperatingHourService.getWeeklySchedule 메서드 구현 후 활성화
        // Map<DayOfWeek, OperatingHourDto> weeklySchedule = operatingHourService.getWeeklySchedule(storeId);
        // return ResponseEntity.ok(weeklySchedule);
        
        // 임시 응답
        return ResponseEntity.ok(Map.of());
    }
} 