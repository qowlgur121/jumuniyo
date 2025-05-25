package com.jumuniyo.service.store;

import com.jumuniyo.domain.store.OperatingHour;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.dto.store.OperatingHourCreateRequestDto;
import com.jumuniyo.dto.store.OperatingHourDto;
import com.jumuniyo.dto.store.OperatingHourUpdateRequestDto;
import com.jumuniyo.repository.store.OperatingHourRepository;
import com.jumuniyo.repository.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 운영시간 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperatingHourServiceImpl implements OperatingHourService {

    private final OperatingHourRepository operatingHourRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public OperatingHourDto createOperatingHour(Long storeId, OperatingHourCreateRequestDto requestDto) {
        Store store = findStoreById(storeId);
        
        // 이미 해당 요일의 운영시간이 있는지 확인
        Optional<OperatingHour> existingOperatingHour = operatingHourRepository
                .findByStoreIdAndDayOfWeek(storeId, requestDto.getDayOfWeek());
        
        if (existingOperatingHour.isPresent()) {
            throw new IllegalArgumentException("해당 요일의 운영시간이 이미 존재합니다: " + requestDto.getDayOfWeek());
        }

        OperatingHour operatingHour = requestDto.toEntity(store);
        OperatingHour savedOperatingHour = operatingHourRepository.save(operatingHour);
        
        log.info("운영시간 생성 완료 - 음식점 ID: {}, 요일: {}", storeId, requestDto.getDayOfWeek());
        return OperatingHourDto.from(savedOperatingHour);
    }

    @Override
    @Transactional
    public List<OperatingHourDto> createOrUpdateOperatingHours(Long storeId, List<OperatingHourCreateRequestDto> requestDtos) {
        Store store = findStoreById(storeId);
        
        List<OperatingHour> operatingHours = requestDtos.stream()
                .map(requestDto -> {
                    // 기존 운영시간이 있는지 확인
                    Optional<OperatingHour> existingOperatingHour = operatingHourRepository
                            .findByStoreIdAndDayOfWeek(storeId, requestDto.getDayOfWeek());
                    
                    if (existingOperatingHour.isPresent()) {
                        // 기존 운영시간 업데이트
                        OperatingHour operatingHour = existingOperatingHour.get();
                        
                        // 운영시간 업데이트
                        if (requestDto.getOpenTime() != null && requestDto.getCloseTime() != null) {
                            operatingHour.updateOperatingTime(
                                    requestDto.getOpenTime(),
                                    requestDto.getCloseTime()
                            );
                        }
                        
                        // 영업 여부 설정
                        if (requestDto.getIsOpen() != null) {
                            if (requestDto.getIsOpen()) {
                                operatingHour.open();
                            } else {
                                operatingHour.close();
                            }
                        }
                        
                        // 브레이크 타임 설정
                        if (requestDto.getIsBreakTime() != null) {
                            if (requestDto.getIsBreakTime()) {
                                if (requestDto.getBreakStartTime() != null && requestDto.getBreakEndTime() != null) {
                                    operatingHour.updateBreakTime(
                                            requestDto.getBreakStartTime(),
                                            requestDto.getBreakEndTime()
                                    );
                                }
                            } else {
                                operatingHour.removeBreakTime();
                            }
                        }
                        
                        return operatingHour;
                    } else {
                        // 새로운 운영시간 생성
                        return requestDto.toEntity(store);
                    }
                })
                .collect(Collectors.toList());

        List<OperatingHour> savedOperatingHours = operatingHourRepository.saveAll(operatingHours);
        
        log.info("운영시간 일괄 저장 완료 - 음식점 ID: {}, 요일 수: {}", storeId, requestDtos.size());
        return savedOperatingHours.stream()
                .map(OperatingHourDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OperatingHourDto> getOperatingHoursByStoreId(Long storeId) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<OperatingHour> operatingHours = operatingHourRepository.findByStoreIdOrderByDayOfWeek(storeId);
        return operatingHours.stream()
                .map(OperatingHourDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public OperatingHourDto getOperatingHourByStoreAndDay(Long storeId, DayOfWeek dayOfWeek) {
        findStoreById(storeId); // 음식점 존재 확인
        
        OperatingHour operatingHour = operatingHourRepository
                .findByStoreIdAndDayOfWeek(storeId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("해당 요일의 운영시간을 찾을 수 없습니다: " + dayOfWeek));
        
        return OperatingHourDto.from(operatingHour);
    }

    @Override
    @Transactional
    public OperatingHourDto updateOperatingHour(Long operatingHourId, OperatingHourUpdateRequestDto requestDto) {
        OperatingHour operatingHour = operatingHourRepository.findById(operatingHourId)
                .orElseThrow(() -> new IllegalArgumentException("운영시간을 찾을 수 없습니다: " + operatingHourId));

        // 운영시간 업데이트
        if (requestDto.getOpenTime() != null && requestDto.getCloseTime() != null) {
            operatingHour.updateOperatingTime(
                    requestDto.getOpenTime(),
                    requestDto.getCloseTime()
            );
        }

        // 영업 여부 설정
        if (requestDto.getIsOpen() != null) {
            if (requestDto.getIsOpen()) {
                operatingHour.open();
            } else {
                operatingHour.close();
            }
        }

        // 브레이크 타임 설정
        if (requestDto.getIsBreakTime() != null) {
            if (requestDto.getIsBreakTime()) {
                if (requestDto.getBreakStartTime() != null && requestDto.getBreakEndTime() != null) {
                    operatingHour.updateBreakTime(
                            requestDto.getBreakStartTime(),
                            requestDto.getBreakEndTime()
                    );
                }
            } else {
                operatingHour.removeBreakTime();
            }
        }

        log.info("운영시간 수정 완료 - ID: {}, 요일: {}", operatingHourId, operatingHour.getDayOfWeek());
        return OperatingHourDto.from(operatingHour);
    }

    @Override
    @Transactional
    public OperatingHourDto updateOperatingHourByStoreAndDay(Long storeId, DayOfWeek dayOfWeek, OperatingHourUpdateRequestDto requestDto) {
        findStoreById(storeId); // 음식점 존재 확인
        
        OperatingHour operatingHour = operatingHourRepository
                .findByStoreIdAndDayOfWeek(storeId, dayOfWeek)
                .orElseThrow(() -> new IllegalArgumentException("해당 요일의 운영시간을 찾을 수 없습니다: " + dayOfWeek));

        return updateOperatingHour(operatingHour.getId(), requestDto);
    }

    @Override
    @Transactional
    public void deleteOperatingHour(Long operatingHourId) {
        OperatingHour operatingHour = operatingHourRepository.findById(operatingHourId)
                .orElseThrow(() -> new IllegalArgumentException("운영시간을 찾을 수 없습니다: " + operatingHourId));

        operatingHourRepository.delete(operatingHour);
        log.info("운영시간 삭제 완료 - ID: {}, 요일: {}", operatingHourId, operatingHour.getDayOfWeek());
    }

    @Override
    @Transactional
    public void deleteAllOperatingHoursByStoreId(Long storeId) {
        Store store = findStoreById(storeId);
        
        operatingHourRepository.deleteByStore(store);
        log.info("음식점의 모든 운영시간 삭제 완료 - 음식점 ID: {}", storeId);
    }

    @Override
    public List<OperatingHourDto> getOpenDaysByStoreId(Long storeId) {
        findStoreById(storeId); // 음식점 존재 확인
        
        List<OperatingHour> openDays = operatingHourRepository.findOpenDaysByStoreId(storeId);
        return openDays.stream()
                .map(OperatingHourDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStoreOpenNow(Long storeId) {
        findStoreById(storeId); // 음식점 존재 확인
        
        DayOfWeek currentDay = DayOfWeek.from(java.time.LocalDate.now());
        LocalTime currentTime = LocalTime.now();
        
        Optional<OperatingHour> operatingHourOpt = operatingHourRepository
                .findByStoreIdAndDayOfWeek(storeId, currentDay);
        
        if (operatingHourOpt.isEmpty()) {
            return false;
        }
        
        OperatingHour operatingHour = operatingHourOpt.get();
        return operatingHour.isOpenAt(currentTime);
    }

    @Override
    public List<OperatingHourDto> getCurrentlyOpenStores(DayOfWeek dayOfWeek) {
        List<OperatingHour> openStores = operatingHourRepository.findCurrentlyOpenStores(dayOfWeek);
        return openStores.stream()
                .map(OperatingHourDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 음식점 조회 (존재하지 않으면 예외 발생)
     */
    private Store findStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("음식점을 찾을 수 없습니다: " + storeId));
    }
} 