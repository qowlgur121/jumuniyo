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

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 배달지역 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
public class DeliveryAreaController {

    private final DeliveryAreaService deliveryAreaService;

    /**
     * 특정 음식점의 배달지역 생성
     */
    @PostMapping("/{storeId}/delivery-areas")
    public ResponseEntity<DeliveryAreaDto> createDeliveryArea(
            @PathVariable Long storeId,
            @Valid @RequestBody DeliveryAreaCreateRequestDto requestDto) {
        
        log.info("배달지역 생성 요청 - 음식점 ID: {}, 지역명: {}", storeId, requestDto.getAreaName());
        DeliveryAreaDto deliveryArea = deliveryAreaService.createDeliveryArea(storeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryArea);
    }

    /**
     * 특정 음식점의 배달지역 일괄 생성/수정
     */
    @PostMapping("/{storeId}/delivery-areas/batch")
    public ResponseEntity<List<DeliveryAreaDto>> createOrUpdateDeliveryAreas(
            @PathVariable Long storeId,
            @Valid @RequestBody List<DeliveryAreaCreateRequestDto> requestDtos) {
        
        log.info("배달지역 일괄 저장 요청 - 음식점 ID: {}, 지역 수: {}", storeId, requestDtos.size());
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.createOrUpdateDeliveryAreas(storeId, requestDtos);
        return ResponseEntity.ok(deliveryAreas);
    }

    /**
     * 특정 음식점의 모든 배달지역 조회
     */
    @GetMapping("/{storeId}/delivery-areas")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAreasByStoreId(@PathVariable Long storeId) {
        log.info("배달지역 목록 조회 요청 - 음식점 ID: {}", storeId);
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.getDeliveryAreasByStoreId(storeId);
        return ResponseEntity.ok(deliveryAreas);
    }

    /**
     * 특정 음식점의 활성화된 배달지역만 조회
     */
    @GetMapping("/{storeId}/delivery-areas/active")
    public ResponseEntity<List<DeliveryAreaDto>> getActiveDeliveryAreasByStoreId(@PathVariable Long storeId) {
        log.info("활성화된 배달지역 조회 요청 - 음식점 ID: {}", storeId);
        List<DeliveryAreaDto> activeDeliveryAreas = deliveryAreaService.getActiveDeliveryAreasByStoreId(storeId);
        return ResponseEntity.ok(activeDeliveryAreas);
    }

    /**
     * 특정 음식점의 특정 지역명 배달지역 조회
     */
    @GetMapping("/{storeId}/delivery-areas/{areaName}")
    public ResponseEntity<DeliveryAreaDto> getDeliveryAreaByStoreAndAreaName(
            @PathVariable Long storeId,
            @PathVariable String areaName) {
        
        log.info("특정 지역 배달지역 조회 요청 - 음식점 ID: {}, 지역명: {}", storeId, areaName);
        DeliveryAreaDto deliveryArea = deliveryAreaService.getDeliveryAreaByStoreAndAreaName(storeId, areaName);
        return ResponseEntity.ok(deliveryArea);
    }

    /**
     * 배달지역 수정 (배달지역 ID로)
     */
    @PutMapping("/{storeId}/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<DeliveryAreaDto> updateDeliveryArea(
            @PathVariable Long storeId,
            @PathVariable Long deliveryAreaId,
            @Valid @RequestBody DeliveryAreaUpdateRequestDto requestDto) {
        
        log.info("배달지역 수정 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        DeliveryAreaDto deliveryArea = deliveryAreaService.updateDeliveryArea(deliveryAreaId, requestDto);
        return ResponseEntity.ok(deliveryArea);
    }

    /**
     * 특정 음식점의 특정 지역명 배달지역 수정
     */
    @PutMapping("/{storeId}/delivery-areas/area/{areaName}")
    public ResponseEntity<DeliveryAreaDto> updateDeliveryAreaByStoreAndAreaName(
            @PathVariable Long storeId,
            @PathVariable String areaName,
            @Valid @RequestBody DeliveryAreaUpdateRequestDto requestDto) {
        
        log.info("특정 지역 배달지역 수정 요청 - 음식점 ID: {}, 지역명: {}", storeId, areaName);
        DeliveryAreaDto deliveryArea = deliveryAreaService.updateDeliveryAreaByStoreAndAreaName(storeId, areaName, requestDto);
        return ResponseEntity.ok(deliveryArea);
    }

    /**
     * 배달지역 삭제
     */
    @DeleteMapping("/{storeId}/delivery-areas/{deliveryAreaId}")
    public ResponseEntity<Void> deleteDeliveryArea(
            @PathVariable Long storeId,
            @PathVariable Long deliveryAreaId) {
        
        log.info("배달지역 삭제 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        deliveryAreaService.deleteDeliveryArea(deliveryAreaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 음식점의 모든 배달지역 삭제
     */
    @DeleteMapping("/{storeId}/delivery-areas")
    public ResponseEntity<Void> deleteAllDeliveryAreasByStoreId(@PathVariable Long storeId) {
        log.info("음식점의 모든 배달지역 삭제 요청 - 음식점 ID: {}", storeId);
        deliveryAreaService.deleteAllDeliveryAreasByStoreId(storeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 주문 금액으로 배달 가능한 지역들 조회
     */
    @GetMapping("/{storeId}/delivery-areas/available")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAvailableAreas(
            @PathVariable Long storeId,
            @RequestParam BigDecimal orderAmount) {
        
        log.info("배달 가능 지역 조회 요청 - 음식점 ID: {}, 주문 금액: {}", storeId, orderAmount);
        List<DeliveryAreaDto> availableAreas = deliveryAreaService.getDeliveryAvailableAreas(storeId, orderAmount);
        return ResponseEntity.ok(availableAreas);
    }

    /**
     * 특정 배달비 이하의 배달지역들 조회
     */
    @GetMapping("/{storeId}/delivery-areas/max-fee")
    public ResponseEntity<List<DeliveryAreaDto>> getDeliveryAreasByMaxFee(
            @PathVariable Long storeId,
            @RequestParam BigDecimal maxDeliveryFee) {
        
        log.info("최대 배달비 기준 배달지역 조회 요청 - 음식점 ID: {}, 최대 배달비: {}", storeId, maxDeliveryFee);
        List<DeliveryAreaDto> deliveryAreas = deliveryAreaService.getDeliveryAreasByMaxFee(storeId, maxDeliveryFee);
        return ResponseEntity.ok(deliveryAreas);
    }

    /**
     * 주문 금액이 해당 지역의 최소 주문 금액을 만족하는지 확인
     */
    @GetMapping("/{storeId}/delivery-areas/{deliveryAreaId}/validate-order")
    public ResponseEntity<Boolean> isOrderAmountValid(
            @PathVariable Long storeId,
            @PathVariable Long deliveryAreaId,
            @RequestParam BigDecimal orderAmount) {
        
        log.info("주문 금액 검증 요청 - 음식점 ID: {}, 배달지역 ID: {}, 주문 금액: {}", storeId, deliveryAreaId, orderAmount);
        boolean isValid = deliveryAreaService.isOrderAmountValid(deliveryAreaId, orderAmount);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 총 결제 금액 계산 (주문 금액 + 배달비)
     */
    @GetMapping("/{storeId}/delivery-areas/{deliveryAreaId}/calculate-total")
    public ResponseEntity<BigDecimal> calculateTotalAmount(
            @PathVariable Long storeId,
            @PathVariable Long deliveryAreaId,
            @RequestParam BigDecimal orderAmount) {
        
        log.info("총 결제 금액 계산 요청 - 음식점 ID: {}, 배달지역 ID: {}, 주문 금액: {}", storeId, deliveryAreaId, orderAmount);
        BigDecimal totalAmount = deliveryAreaService.calculateTotalAmount(deliveryAreaId, orderAmount);
        return ResponseEntity.ok(totalAmount);
    }

    /**
     * 특정 지역명으로 배달 가능한 음식점들 검색
     */
    @GetMapping("/delivery-areas/search")
    public ResponseEntity<List<DeliveryAreaDto>> searchAvailableStoresByAreaName(@RequestParam String areaName) {
        log.info("지역명으로 음식점 검색 요청 - 지역명: {}", areaName);
        List<DeliveryAreaDto> availableStores = deliveryAreaService.searchAvailableStoresByAreaName(areaName);
        return ResponseEntity.ok(availableStores);
    }

    /**
     * 배달지역 활성화/비활성화
     */
    @PatchMapping("/{storeId}/delivery-areas/{deliveryAreaId}/toggle-status")
    public ResponseEntity<DeliveryAreaDto> toggleDeliveryAreaStatus(
            @PathVariable Long storeId,
            @PathVariable Long deliveryAreaId) {
        
        log.info("배달지역 활성화 상태 토글 요청 - 음식점 ID: {}, 배달지역 ID: {}", storeId, deliveryAreaId);
        DeliveryAreaDto deliveryArea = deliveryAreaService.toggleDeliveryAreaStatus(deliveryAreaId);
        return ResponseEntity.ok(deliveryArea);
    }
} 