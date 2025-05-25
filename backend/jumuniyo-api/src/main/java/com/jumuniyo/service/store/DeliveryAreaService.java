package com.jumuniyo.service.store;

import com.jumuniyo.dto.store.DeliveryAreaCreateRequestDto;
import com.jumuniyo.dto.store.DeliveryAreaDto;
import com.jumuniyo.dto.store.DeliveryAreaUpdateRequestDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 배달지역 관리 서비스 인터페이스
 */
public interface DeliveryAreaService {

    /**
     * 특정 음식점의 배달지역 생성
     */
    DeliveryAreaDto createDeliveryArea(Long storeId, DeliveryAreaCreateRequestDto requestDto);

    /**
     * 특정 음식점의 배달지역 일괄 생성/수정
     */
    List<DeliveryAreaDto> createOrUpdateDeliveryAreas(Long storeId, List<DeliveryAreaCreateRequestDto> requestDtos);

    /**
     * 특정 음식점의 모든 배달지역 조회
     */
    List<DeliveryAreaDto> getDeliveryAreasByStoreId(Long storeId);

    /**
     * 특정 음식점의 활성화된 배달지역만 조회
     */
    List<DeliveryAreaDto> getActiveDeliveryAreasByStoreId(Long storeId);

    /**
     * 특정 음식점의 특정 지역명 배달지역 조회
     */
    DeliveryAreaDto getDeliveryAreaByStoreAndAreaName(Long storeId, String areaName);

    /**
     * 배달지역 수정
     */
    DeliveryAreaDto updateDeliveryArea(Long deliveryAreaId, DeliveryAreaUpdateRequestDto requestDto);

    /**
     * 특정 음식점의 특정 지역명 배달지역 수정
     */
    DeliveryAreaDto updateDeliveryAreaByStoreAndAreaName(Long storeId, String areaName, DeliveryAreaUpdateRequestDto requestDto);

    /**
     * 배달지역 삭제
     */
    void deleteDeliveryArea(Long deliveryAreaId);

    /**
     * 특정 음식점의 모든 배달지역 삭제
     */
    void deleteAllDeliveryAreasByStoreId(Long storeId);

    /**
     * 특정 주문 금액으로 배달 가능한 지역들 조회
     */
    List<DeliveryAreaDto> getDeliveryAvailableAreas(Long storeId, BigDecimal orderAmount);

    /**
     * 특정 배달비 이하의 배달지역들 조회
     */
    List<DeliveryAreaDto> getDeliveryAreasByMaxFee(Long storeId, BigDecimal maxDeliveryFee);

    /**
     * 주문 금액이 해당 지역의 최소 주문 금액을 만족하는지 확인
     */
    boolean isOrderAmountValid(Long deliveryAreaId, BigDecimal orderAmount);

    /**
     * 총 결제 금액 계산 (주문 금액 + 배달비)
     */
    BigDecimal calculateTotalAmount(Long deliveryAreaId, BigDecimal orderAmount);

    /**
     * 특정 지역명으로 배달 가능한 음식점들 검색
     */
    List<DeliveryAreaDto> searchAvailableStoresByAreaName(String areaName);

    /**
     * 배달지역 활성화/비활성화
     */
    DeliveryAreaDto toggleDeliveryAreaStatus(Long deliveryAreaId);
} 