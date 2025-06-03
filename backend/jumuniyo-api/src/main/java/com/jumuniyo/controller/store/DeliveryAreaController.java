package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.DeliveryAreaCreateRequestDto;
import com.jumuniyo.dto.store.DeliveryAreaDto;
import com.jumuniyo.dto.store.DeliveryAreaUpdateRequestDto;
import com.jumuniyo.service.store.DeliveryAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 배달지역 관리 REST API Controller
 */
@Tag(name = "배달지역 관리 API (사업자용)", description = "매장의 배달지역 생성, 수정, 조회, 삭제 및 배달비 관리 API (사업자 인증 필요 - X-Owner-Id 헤더 사용)")
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class DeliveryAreaController {

    private final DeliveryAreaService deliveryAreaService;

    @Operation(
        summary = "배달지역 생성",
        description = "특정 매장의 새로운 배달지역을 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "배달지역 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/delivery-areas")
    public ResponseEntity<DeliveryAreaDto> createDeliveryArea(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody DeliveryAreaCreateRequestDto requestDto) {
        
        log.info("배달지역 생성 요청 - 음식점 ID: {}, 지역명: {}", storeId, requestDto.getAreaName());
        DeliveryAreaDto deliveryArea = deliveryAreaService.createDeliveryArea(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryArea);
    }

    @Operation(
        summary = "배달지역 일괄 생성/수정",
        description = "특정 매장의 배달지역들을 일괄로 생성하거나 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 일괄 저장 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/delivery-areas/batch")
    public ResponseEntity<List<DeliveryAreaDto>> createOrUpdateDeliveryAreas(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody List<DeliveryAreaCreateRequestDto> requestDtos) {
        
        log.info("배달지역 일괄 저장 요청 - 음식점 ID: {}, 지역 수: {}", storeId, requestDtos.size());
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.createOrUpdateDeliveryAreas(storeId, requestDtos);
        return ResponseEntity.ok(deliveryAreas);
    }

    @Operation(
        summary = "매장 배달지역 목록 조회",
        description = "특정 매장의 모든 배달지역 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAreasByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("배달지역 목록 조회 요청 - 음식점 ID: {}", storeId);
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.getDeliveryAreasByStoreId(storeId);
        return ResponseEntity.ok(deliveryAreas);
    }

    @Operation(
        summary = "활성화된 배달지역 조회",
        description = "특정 매장의 활성화된 배달지역만 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "활성화된 배달지역 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/active")
    public ResponseEntity<List<DeliveryAreaDto>> getActiveDeliveryAreasByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("활성화된 배달지역 조회 요청 - 음식점 ID: {}", storeId);
        List<DeliveryAreaDto> activeDeliveryAreas = deliveryAreaService.getActiveDeliveryAreasByStoreId(storeId);
        return ResponseEntity.ok(activeDeliveryAreas);
    }

    @Operation(
        summary = "지역명으로 배달지역 조회",
        description = "특정 매장의 특정 지역명에 해당하는 배달지역을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장 또는 배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/{areaName}")
    public ResponseEntity<DeliveryAreaDto> getDeliveryAreaByStoreAndAreaName(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "지역명", required = true, example = "강남구")
            @PathVariable String areaName) {
        
        log.info("특정 지역 배달지역 조회 요청 - 음식점 ID: {}, 지역명: {}", storeId, areaName);
        DeliveryAreaDto deliveryArea = deliveryAreaService.getDeliveryAreaByStoreAndAreaName(storeId, areaName);
        return ResponseEntity.ok(deliveryArea);
    }

    @Operation(
        summary = "배달지역 수정",
        description = "배달지역 ID로 특정 배달지역의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{storeId}/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<DeliveryAreaDto> updateDeliveryArea(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "배달지역 ID", required = true, example = "1")
            @PathVariable Long deliveryAreaId,
            @Valid @RequestBody DeliveryAreaUpdateRequestDto requestDto) {
        
        log.info("배달지역 수정 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        DeliveryAreaDto deliveryArea = deliveryAreaService.updateDeliveryArea(deliveryAreaId, requestDto);
        return ResponseEntity.ok(deliveryArea);
    }

    @Operation(
        summary = "지역명으로 배달지역 수정",
        description = "매장 ID와 지역명으로 특정 배달지역의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{storeId}/delivery-areas/area/{areaName}")
    public ResponseEntity<DeliveryAreaDto> updateDeliveryAreaByStoreAndAreaName(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "지역명", required = true, example = "강남구")
            @PathVariable String areaName,
            @Valid @RequestBody DeliveryAreaUpdateRequestDto requestDto) {
        
        log.info("특정 지역 배달지역 수정 요청 - 음식점 ID: {}, 지역명: {}", storeId, areaName);
        DeliveryAreaDto deliveryArea = deliveryAreaService.updateDeliveryAreaByStoreAndAreaName(storeId, areaName, requestDto);
        return ResponseEntity.ok(deliveryArea);
    }

    @Operation(
        summary = "배달지역 삭제",
        description = "특정 배달지역을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "배달지역 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<Void> deleteDeliveryArea(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "배달지역 ID", required = true, example = "1")
            @PathVariable Long deliveryAreaId) {
        
        log.info("배달지역 삭제 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        deliveryAreaService.deleteDeliveryArea(deliveryAreaId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "매장 배달지역 전체 삭제",
        description = "특정 매장의 모든 배달지역을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "배달지역 전체 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}/delivery-areas")
    public ResponseEntity<Void> deleteAllDeliveryAreasByStoreId(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점의 모든 배달지역 삭제 요청 - 음식점 ID: {}", storeId);
        deliveryAreaService.deleteAllDeliveryAreasByStoreId(storeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "배달 가능 지역 조회",
        description = "특정 주문 금액으로 배달 가능한 지역들을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달 가능 지역 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 주문 금액"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/available")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAvailableAreas(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "주문 금액", required = true, example = "15000")
            @RequestParam BigDecimal orderAmount) {
        
        log.info("배달 가능 지역 조회 요청 - 음식점 ID: {}, 주문 금액: {}", storeId, orderAmount);
        List<DeliveryAreaDto> availableAreas = deliveryAreaService.getDeliveryAvailableAreas(storeId, orderAmount);
        return ResponseEntity.ok(availableAreas);
    }

    @Operation(
        summary = "최대 배달비 기준 지역 조회",
        description = "특정 배달비 이하의 배달지역들을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 배달비"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/max-fee")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAreasByMaxFee(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "최대 배달비", required = true, example = "3000")
            @RequestParam BigDecimal maxDeliveryFee) {
        
        log.info("최대 배달비 기준 배달지역 조회 요청 - 음식점 ID: {}, 최대 배달비: {}", storeId, maxDeliveryFee);
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.getDeliveryAreasByMaxFee(storeId, maxDeliveryFee);
        return ResponseEntity.ok(deliveryAreas);
    }

    @Operation(
        summary = "주문 금액 검증",
        description = "주문 금액이 해당 지역의 최소 주문 금액을 만족하는지 확인합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 금액 검증 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 주문 금액"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/{deliveryAreaId}/validate-order")
    public ResponseEntity<Boolean> isOrderAmountValid(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "배달지역 ID", required = true, example = "1")
            @PathVariable Long deliveryAreaId,
            @Parameter(description = "주문 금액", required = true, example = "15000")
            @RequestParam BigDecimal orderAmount) {
        
        log.info("주문 금액 검증 요청 - 음식점 ID: {}, 배달지역 ID: {}, 주문 금액: {}", storeId, deliveryAreaId, orderAmount);
        boolean isValid = deliveryAreaService.isOrderAmountValid(deliveryAreaId, orderAmount);
        return ResponseEntity.ok(isValid);
    }

    @Operation(
        summary = "총 결제 금액 계산",
        description = "주문 금액에 배달비를 포함한 총 결제 금액을 계산합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "총 결제 금액 계산 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 주문 금액"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}/delivery-areas/{deliveryAreaId}/calculate-total")
    public ResponseEntity<BigDecimal> calculateTotalAmount(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "배달지역 ID", required = true, example = "1")
            @PathVariable Long deliveryAreaId,
            @Parameter(description = "주문 금액", required = true, example = "15000")
            @RequestParam BigDecimal orderAmount) {
        
        log.info("총 결제 금액 계산 요청 - 음식점 ID: {}, 배달지역 ID: {}, 주문 금액: {}", storeId, deliveryAreaId, orderAmount);
        BigDecimal totalAmount = deliveryAreaService.calculateTotalAmount(deliveryAreaId, orderAmount);
        return ResponseEntity.ok(totalAmount);
    }

    @Operation(
        summary = "지역명으로 매장 검색",
        description = "특정 지역명으로 배달 가능한 매장들을 검색합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 지역명"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/delivery-areas/search")
    public ResponseEntity<List<DeliveryAreaDto>> searchAvailableStoresByAreaName(
            @Parameter(description = "지역명", required = true, example = "강남구")
            @RequestParam String areaName) {
        log.info("지역명으로 음식점 검색 요청 - 지역명: {}", areaName);
        List<DeliveryAreaDto> availableStores = deliveryAreaService.searchAvailableStoresByAreaName(areaName);
        return ResponseEntity.ok(availableStores);
    }

    @Operation(
        summary = "배달지역 활성화/비활성화",
        description = "배달지역의 활성화 상태를 토글합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "배달지역 상태 변경 성공"),
        @ApiResponse(responseCode = "404", description = "배달지역을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{storeId}/delivery-areas/{deliveryAreaId}/toggle-status")
    public ResponseEntity<DeliveryAreaDto> toggleDeliveryAreaStatus(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "배달지역 ID", required = true, example = "1")
            @PathVariable Long deliveryAreaId) {
        
        log.info("배달지역 활성화 상태 토글 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        DeliveryAreaDto deliveryArea = deliveryAreaService.toggleDeliveryAreaStatus(deliveryAreaId);
        return ResponseEntity.ok(deliveryArea);
    }
} 