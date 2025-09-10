package com.jumuniyo.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지네이션된 응답을 위한 공통 DTO
 * 프론트엔드에서 필요한 페이지네이션 메타데이터를 모두 포함합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponseDto<T> {
    
    // 실제 데이터 내용
    private List<T> content;
    
    // 페이지네이션 정보
    private PaginationMetadata pagination;
    
    /**
     * Spring Data Page 객체를 PaginatedResponseDto로 변환
     * 
     * @param page Spring Data Page 객체
     * @param <T> 데이터 타입
     * @return PaginatedResponseDto 객체
     */
    public static <T> PaginatedResponseDto<T> of(Page<T> page) {
        return PaginatedResponseDto.<T>builder()
                .content(page.getContent())
                .pagination(PaginationMetadata.of(page))
                .build();
    }
    
    /**
     * 페이지네이션 메타데이터 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        
        // 현재 페이지 번호 (0부터 시작)
        private int page;
        
        // 페이지 크기
        private int size;
        
        // 총 요소 개수
        private long totalElements;
        
        // 총 페이지 수
        private int totalPages;
        
        // 첫 번째 페이지 여부
        private boolean first;
        
        // 마지막 페이지 여부
        private boolean last;
        
        // 빈 페이지 여부
        private boolean empty;
        
        // 현재 페이지의 요소 개수
        private int numberOfElements;
        
        // 정렬 정보
        private SortMetadata sort;
        
        /**
         * Spring Data Page 객체에서 PaginationMetadata 생성
         * 
         * @param page Spring Data Page 객체
         * @return PaginationMetadata 객체
         */
        public static PaginationMetadata of(Page<?> page) {
            return PaginationMetadata.builder()
                    .page(page.getNumber())
                    .size(page.getSize())
                    .totalElements(page.getTotalElements())
                    .totalPages(page.getTotalPages())
                    .first(page.isFirst())
                    .last(page.isLast())
                    .empty(page.isEmpty())
                    .numberOfElements(page.getNumberOfElements())
                    .sort(SortMetadata.of(page.getSort()))
                    .build();
        }
    }
    
    /**
     * 정렬 정보 메타데이터 클래스
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortMetadata {
        
        // 정렬 여부
        private boolean sorted;
        
        // 비정렬 여부
        private boolean unsorted;
        
        // 빈 정렬 여부
        private boolean empty;
        
        /**
         * Spring Data Sort 객체에서 SortMetadata 생성
         * 
         * @param sort Spring Data Sort 객체
         * @return SortMetadata 객체
         */
        public static SortMetadata of(org.springframework.data.domain.Sort sort) {
            return SortMetadata.builder()
                    .sorted(sort.isSorted())
                    .unsorted(sort.isUnsorted())
                    .empty(sort.isEmpty())
                    .build();
        }
    }
} 