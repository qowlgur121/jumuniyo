package com.jumuniyo.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * 페이지네이션 관련 유틸리티 클래스
 * 대용량 데이터 처리와 일관된 페이지네이션 로직을 제공합니다.
 */
@Slf4j
public class PaginationUtils {

    // 최대 페이지 크기 제한 (DoS 공격 방지)
    public static final int MAX_PAGE_SIZE = 100;
    
    // 기본 페이지 크기
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    // 최대 페이지 번호 (성능상 제한)
    public static final int MAX_PAGE_NUMBER = 1000;

    private PaginationUtils() {
        // 유틸리티 클래스는 인스턴스화 방지
    }

    /**
     * Pageable 객체의 유효성을 검증하고 안전한 값으로 조정합니다.
     * 
     * @param pageable 검증할 Pageable 객체
     * @return 검증되고 조정된 Pageable 객체
     */
    public static Pageable validateAndSanitizePageable(Pageable pageable) {
        if (pageable == null) {
            log.warn("Pageable is null, using default values");
            return org.springframework.data.domain.PageRequest.of(0, DEFAULT_PAGE_SIZE);
        }

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        // 페이지 번호 검증 (음수 방지, 최대값 제한)
        if (pageNumber < 0) {
            log.warn("Invalid page number: {}, setting to 0", pageNumber);
            pageNumber = 0;
        } else if (pageNumber > MAX_PAGE_NUMBER) {
            log.warn("Page number {} exceeds maximum {}, setting to {}", pageNumber, MAX_PAGE_NUMBER, MAX_PAGE_NUMBER);
            pageNumber = MAX_PAGE_NUMBER;
        }

        // 페이지 크기 검증 (최소값 1, 최대값 제한)
        if (pageSize <= 0) {
            log.warn("Invalid page size: {}, setting to default {}", pageSize, DEFAULT_PAGE_SIZE);
            pageSize = DEFAULT_PAGE_SIZE;
        } else if (pageSize > MAX_PAGE_SIZE) {
            log.warn("Page size {} exceeds maximum {}, setting to {}", pageSize, MAX_PAGE_SIZE, MAX_PAGE_SIZE);
            pageSize = MAX_PAGE_SIZE;
        }

        // 기존 Pageable과 값이 다르면 새로운 PageRequest 생성
        if (pageNumber != pageable.getPageNumber() || pageSize != pageable.getPageSize()) {
            return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize, pageable.getSort());
        }

        return pageable;
    }

    /**
     * 리스트를 안전하게 페이징 처리합니다.
     * 인덱스 오버플로우를 방지하고 빈 리스트도 안전하게 처리합니다.
     * 
     * @param <T> 리스트 요소 타입
     * @param fullList 전체 리스트
     * @param pageable 페이징 정보
     * @return 페이징된 Page 객체
     */
    public static <T> Page<T> createPageFromList(List<T> fullList, Pageable pageable) {
        if (fullList == null || fullList.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Pageable validatedPageable = validateAndSanitizePageable(pageable);
        
        int totalElements = fullList.size();
        int startIndex = (int) validatedPageable.getOffset();
        
        // 시작 인덱스가 전체 크기를 초과하는 경우
        if (startIndex >= totalElements) {
            log.warn("Start index {} exceeds total elements {}, returning empty page", startIndex, totalElements);
            return new PageImpl<>(Collections.emptyList(), validatedPageable, totalElements);
        }
        
        int endIndex = Math.min(startIndex + validatedPageable.getPageSize(), totalElements);
        List<T> pageContent = fullList.subList(startIndex, endIndex);
        
        return new PageImpl<>(pageContent, validatedPageable, totalElements);
    }

    /**
     * 효율적인 스트림 기반 페이징을 위한 시작/끝 인덱스를 계산합니다.
     * 
     * @param pageable 페이징 정보
     * @param totalElements 전체 요소 수
     * @return [startIndex, endIndex] 배열
     */
    public static int[] calculatePageBounds(Pageable pageable, int totalElements) {
        Pageable validatedPageable = validateAndSanitizePageable(pageable);
        
        int startIndex = (int) validatedPageable.getOffset();
        if (startIndex >= totalElements) {
            return new int[]{totalElements, totalElements}; // 빈 범위
        }
        
        int endIndex = Math.min(startIndex + validatedPageable.getPageSize(), totalElements);
        return new int[]{startIndex, endIndex};
    }

    /**
     * 스트림 기반 처리를 위한 효율적인 페이징 범위를 확인합니다.
     * 
     * @param pageable 페이징 정보
     * @param totalElements 전체 요소 수
     * @return 해당 페이지에 데이터가 있는지 여부
     */
    public static boolean hasDataInPage(Pageable pageable, int totalElements) {
        if (totalElements == 0) {
            return false;
        }
        
        Pageable validatedPageable = validateAndSanitizePageable(pageable);
        int startIndex = (int) validatedPageable.getOffset();
        
        return startIndex < totalElements;
    }

    /**
     * 페이징 성능을 위한 통계 로깅
     * 
     * @param operation 작업명
     * @param totalElements 전체 요소 수
     * @param pageable 페이징 정보
     * @param processingTimeMs 처리 시간 (밀리초)
     */
    public static void logPaginationStats(String operation, int totalElements, Pageable pageable, long processingTimeMs) {
        if (log.isDebugEnabled()) {
            log.debug("Pagination Stats - Operation: {}, Total: {}, Page: {}, Size: {}, Processing Time: {}ms",
                    operation, totalElements, pageable.getPageNumber(), pageable.getPageSize(), processingTimeMs);
        }
        
        // 성능 경고 (500ms 이상)
        if (processingTimeMs > 500) {
            log.warn("Slow pagination detected - Operation: {}, Total: {}, Time: {}ms", 
                    operation, totalElements, processingTimeMs);
        }
    }

    /**
     * 페이지네이션 최적화를 위한 권장사항을 확인합니다.
     * 
     * @param totalElements 전체 요소 수
     * @param pageable 페이징 정보
     * @return 최적화 권장사항이 있는지 여부
     */
    public static boolean shouldOptimizePagination(int totalElements, Pageable pageable) {
        // 대용량 데이터에서 뒤쪽 페이지 접근 시 최적화 필요
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        
        // 전체 데이터가 10,000개 이상이고, 100페이지 이후 접근 시
        if (totalElements > 10000 && pageNumber > 100) {
            log.info("Large dataset pagination detected - Total: {}, Page: {}, Consider implementing cursor-based pagination", 
                    totalElements, pageNumber);
            return true;
        }
        
        return false;
    }

    /**
     * 메모리 효율적인 페이징을 위한 청크 크기를 계산합니다.
     * 
     * @param totalElements 전체 요소 수
     * @param targetPageSize 목표 페이지 크기
     * @return 최적화된 청크 크기
     */
    public static int calculateOptimalChunkSize(int totalElements, int targetPageSize) {
        // 전체 데이터가 적으면 한 번에 처리
        if (totalElements <= 1000) {
            return totalElements;
        }
        
        // 목표 페이지 크기의 2-5배를 청크 크기로 사용
        int chunkSize = targetPageSize * 3;
        
        // 최대 청크 크기 제한 (메모리 보호)
        return Math.min(chunkSize, 5000);
    }
} 