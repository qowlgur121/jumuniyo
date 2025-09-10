package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.DeliveryArea;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.dto.store.DeliveryAreaCreateRequestDto;
import com.jumuniyo.dto.store.DeliveryAreaDto;
import com.jumuniyo.dto.store.DeliveryAreaUpdateRequestDto;
import com.jumuniyo.repository.store.DeliveryAreaRepository;
import com.jumuniyo.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 배달지역 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryAreaServiceImpl implements DeliveryAreaService {

    private final DeliveryAreaRepository deliveryAreaRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public DeliveryAreaDto createDeliveryArea(Long storeId, DeliveryAreaCreateRequestDto requestDto) {
        Store store = findStoreById(storeId);
        
        // 이미 해당 지역명의 배달지역이 있는지 확인
        Optional<DeliveryArea> existingDeliveryArea = deliveryAreaRepository
                .findByStoreIdAndAreaName(storeId, requestDto.getAreaName());
        
        if (existingDeliveryArea.isPresent()) {
            throw new IllegalArgumentException("해당 지역의 배달지역이 이미 존재합니다: " + requestDto.getAreaName());
        }

        DeliveryArea deliveryArea = requestDto.toEntity(store);
        DeliveryArea savedDeliveryArea = deliveryAreaRepository.save(deliveryArea);
        
        log.info("배달지역 생성 완료 - 음식점 ID: {}, 지역명: {}", storeId, requestDto.getAreaName());
        return DeliveryAreaDto.from(savedDeliveryArea);
    }

    @Override
    @Transactional
    public List<DeliveryAreaDto> createOrUpdateDeliveryAreas(Long storeId, List<DeliveryAreaCreateRequestDto> requestDtos) {
        Store store = findStoreById(storeId);
        
        List<DeliveryArea> deliveryAreas = requestDtos.stream()
                .map(requestDto -> {
                    // 기존 배달지역이 있는지 확인
                    Optional<DeliveryArea> existingDeliveryArea = deliveryAreaRepository
                            .findByStoreIdAndAreaName(storeId, requestDto.getAreaName());
                    
                    if (existingDeliveryArea.isPresent()) {
                        // 기존 배달지역 업데이트
                        DeliveryArea deliveryArea = existingDeliveryArea.get();
                        
                        // 기본 정보 업데이트
                        if (requestDto.getAreaName() != null || requestDto.getDetailAddress() != null || requestDto.getDescription() != null) {
                            deliveryArea.updateAreaInfo(
                                    requestDto.getAreaName() != null ? requestDto.getAreaName() : deliveryArea.getAreaName(),
                                    requestDto.getDetailAddress(),
                                    requestDto.getDescription()
                            );
                        }
                        
                        // 배달비 업데이트
                        if (requestDto.getDeliveryFee() != null) {
                            deliveryArea.updateDeliveryFee(requestDto.getDeliveryFee());
                        }
                        
                        // 최소 주문 금액 업데이트
                        if (requestDto.getMinimumOrderAmount() != null) {
                            deliveryArea.updateMinimumOrderAmount(requestDto.getMinimumOrderAmount());
                        }
                        
                        // 배달 시간 업데이트
                        if (requestDto.getDeliveryTimeMinutes() != null) {
                            deliveryArea.updateDeliveryTime(requestDto.getDeliveryTimeMinutes());
                        }
                        
                        // 활성화 상태 업데이트
                        if (requestDto.getIsActive() != null) {
                            if (requestDto.getIsActive()) {
                                deliveryArea.activate();
                            } else {
                                deliveryArea.deactivate();
                            }
                        }
                        
                        return deliveryArea;
                    } else {
                        // 새로운 배달지역 생성
                        return requestDto.toEntity(store);
                    }
                })
                .collect(Collectors.toList());

        List<DeliveryArea> savedDeliveryAreas = deliveryAreaRepository.saveAll(deliveryAreas);
        
        log.info("배달지역 일괄 저장 완료 - 음식점 ID: {}, 지역 수: {}", storeId, requestDtos.size());
        return savedDeliveryAreas.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryAreaDto> getDeliveryAreasByStoreId(Long storeId) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<DeliveryArea> deliveryAreas = deliveryAreaRepository.findByStoreIdOrderByAreaName(storeId);
        return deliveryAreas.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryAreaDto> getActiveDeliveryAreasByStoreId(Long storeId) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<DeliveryArea> activeDeliveryAreas = deliveryAreaRepository.findActiveDeliveryAreasByStoreId(storeId);
        return activeDeliveryAreas.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryAreaDto getDeliveryAreaByStoreAndAreaName(Long storeId, String areaName) {
        findStoreById(storeId); // 음식점 존재 확인
        
        DeliveryArea deliveryArea = deliveryAreaRepository
                .findByStoreIdAndAreaName(storeId, areaName)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역의 배달지역을 찾을 수 없습니다: " + areaName));
        
        return DeliveryAreaDto.from(deliveryArea);
    }

    @Override
    @Transactional
    public DeliveryAreaDto updateDeliveryArea(Long deliveryAreaId, DeliveryAreaUpdateRequestDto requestDto) {
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(deliveryAreaId)
                .orElseThrow(() -> new IllegalArgumentException("배달지역을 찾을 수 없습니다: " + deliveryAreaId));

        // 기본 정보 업데이트
        if (requestDto.getAreaName() != null || requestDto.getDetailAddress() != null || requestDto.getDescription() != null) {
            deliveryArea.updateAreaInfo(
                    requestDto.getAreaName() != null ? requestDto.getAreaName() : deliveryArea.getAreaName(),
                    requestDto.getDetailAddress() != null ? requestDto.getDetailAddress() : deliveryArea.getDetailAddress(),
                    requestDto.getDescription() != null ? requestDto.getDescription() : deliveryArea.getDescription()
            );
        }

        // 배달비 업데이트
        if (requestDto.getDeliveryFee() != null) {
            deliveryArea.updateDeliveryFee(requestDto.getDeliveryFee());
        }

        // 최소 주문 금액 업데이트
        if (requestDto.getMinimumOrderAmount() != null) {
            deliveryArea.updateMinimumOrderAmount(requestDto.getMinimumOrderAmount());
        }

        // 배달 시간 업데이트
        if (requestDto.getDeliveryTimeMinutes() != null) {
            deliveryArea.updateDeliveryTime(requestDto.getDeliveryTimeMinutes());
        }

        // 활성화 상태 업데이트
        if (requestDto.getIsActive() != null) {
            if (requestDto.getIsActive()) {
                deliveryArea.activate();
            } else {
                deliveryArea.deactivate();
            }
        }

        log.info("배달지역 수정 완료 - ID: {}, 지역명: {}", deliveryAreaId, deliveryArea.getAreaName());
        return DeliveryAreaDto.from(deliveryArea);
    }

    @Override
    @Transactional
    public DeliveryAreaDto updateDeliveryAreaByStoreAndAreaName(Long storeId, String areaName, DeliveryAreaUpdateRequestDto requestDto) {
        findStoreById(storeId); // 음식점 존재 확인
        
        DeliveryArea deliveryArea = deliveryAreaRepository
                .findByStoreIdAndAreaName(storeId, areaName)
                .orElseThrow(() -> new IllegalArgumentException("해당 지역의 배달지역을 찾을 수 없습니다: " + areaName));

        return updateDeliveryArea(deliveryArea.getId(), requestDto);
    }

    @Override
    @Transactional
    public void deleteDeliveryArea(Long deliveryAreaId) {
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(deliveryAreaId)
                .orElseThrow(() -> new IllegalArgumentException("배달지역을 찾을 수 없습니다: " + deliveryAreaId));

        deliveryAreaRepository.delete(deliveryArea);
        log.info("배달지역 삭제 완료 - ID: {}, 지역명: {}", deliveryAreaId, deliveryArea.getAreaName());
    }

    @Override
    @Transactional
    public void deleteAllDeliveryAreasByStoreId(Long storeId) {
        Store store = findStoreById(storeId);
        
        deliveryAreaRepository.deleteByStore(store);
        log.info("음식점의 모든 배달지역 삭제 완료 - 음식점 ID: {}", storeId);
    }

    @Override
    public List<DeliveryAreaDto> getDeliveryAvailableAreas(Long storeId, BigDecimal orderAmount) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<DeliveryArea> availableAreas = deliveryAreaRepository.findDeliveryAvailableAreas(storeId, orderAmount);
        return availableAreas.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeliveryAreaDto> getDeliveryAreasByMaxFee(Long storeId, BigDecimal maxDeliveryFee) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<DeliveryArea> deliveryAreas = deliveryAreaRepository.findByMaxDeliveryFee(storeId, maxDeliveryFee);
        return deliveryAreas.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isOrderAmountValid(Long deliveryAreaId, BigDecimal orderAmount) {
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(deliveryAreaId)
                .orElseThrow(() -> new IllegalArgumentException("배달지역을 찾을 수 없습니다: " + deliveryAreaId));
        
        return deliveryArea.isOrderAmountValid(orderAmount);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long deliveryAreaId, BigDecimal orderAmount) {
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(deliveryAreaId)
                .orElseThrow(() -> new IllegalArgumentException("배달지역을 찾을 수 없습니다: " + deliveryAreaId));
        
        return deliveryArea.calculateTotalAmount(orderAmount);
    }

    @Override
    public List<DeliveryAreaDto> searchAvailableStoresByAreaName(String areaName) {
        List<DeliveryArea> availableStores = deliveryAreaRepository.findAvailableStoresByAreaName(areaName);
        return availableStores.stream()
                .map(DeliveryAreaDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeliveryAreaDto toggleDeliveryAreaStatus(Long deliveryAreaId) {
        DeliveryArea deliveryArea = deliveryAreaRepository.findById(deliveryAreaId)
                .orElseThrow(() -> new IllegalArgumentException("배달지역을 찾을 수 없습니다: " + deliveryAreaId));

        if (deliveryArea.getIsActive()) {
            deliveryArea.deactivate();
            log.info("배달지역 비활성화 - ID: {}, 지역명: {}", deliveryAreaId, deliveryArea.getAreaName());
        } else {
            deliveryArea.activate();
            log.info("배달지역 활성화 - ID: {}, 지역명: {}", deliveryAreaId, deliveryArea.getAreaName());
        }

        return DeliveryAreaDto.from(deliveryArea);
    }

    /**
     * 음식점 조회 (존재하지 않으면 예외 발생)
     */
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다: " + storeId));
    }
} 