package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import com.jumuniyo.service.store.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;

    /**
     * 음식점 등록
     */
    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreCreateRequestDto requestDto) {
        log.info("음식점 등록 요청: {}", requestDto.getName());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        StoreResponseDto responseDto = storeService.createStore(requestDto, userEmail);
        
        log.info("음식점 등록 완료: ID={}, 이름={}", responseDto.getId(), responseDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 음식점 상세 조회
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponseDto> getStore(@PathVariable Long storeId) {
        log.info("음식점 조회 요청: ID={}", storeId);
        
        StoreResponseDto responseDto = storeService.getStoreById(storeId);
        
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 음식점 수정
     */
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponseDto> updateStore(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        log.info("음식점 수정 요청: ID={}, 이름={}", storeId, requestDto.getName());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        StoreResponseDto responseDto = storeService.updateStore(storeId, requestDto, userEmail);
        
        log.info("음식점 수정 완료: ID={}", storeId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 음식점 삭제 (비활성화)
     */
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Map<String, String>> deleteStore(@PathVariable Long storeId) {
        log.info("음식점 삭제 요청: ID={}", storeId);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        storeService.deleteStore(storeId, userEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점이 성공적으로 삭제되었습니다.");
        
        log.info("음식점 삭제 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 음식점 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<StoreResponseDto>> getMyStores(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("내 음식점 목록 조회 요청");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        Page<StoreResponseDto> stores = storeService.getStoresByOwner(userEmail, pageable);
        
        return ResponseEntity.ok(stores);
    }

    /**
     * 음식점 검색/목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String area,
            @RequestParam(required = false, defaultValue = "rating") String sortBy,
            @RequestParam(required = false, defaultValue = "false") Boolean approvedOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("음식점 검색 요청: keyword={}, categoryId={}, area={}, sortBy={}", 
                keyword, categoryId, area, sortBy);
        
        Page<StoreResponseDto> stores = storeService.searchStores(
                keyword, categoryId, area, sortBy, approvedOnly, pageable);
        
        return ResponseEntity.ok(stores);
    }

    /**
     * 음식점 승인 (관리자용)
     */
    @PostMapping("/{storeId}/approve")
    public ResponseEntity<Map<String, String>> approveStore(@PathVariable Long storeId) {
        log.info("음식점 승인 요청: ID={}", storeId);
        
        storeService.approveStore(storeId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점이 승인되었습니다.");
        
        log.info("음식점 승인 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 음식점 승인 거부 (관리자용)
     */
    @PostMapping("/{storeId}/reject")
    public ResponseEntity<Map<String, String>> rejectStore(@PathVariable Long storeId) {
        log.info("음식점 승인 거부 요청: ID={}", storeId);
        
        storeService.rejectStore(storeId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점 승인이 거부되었습니다.");
        
        log.info("음식점 승인 거부 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 음식점 영업 상태 토글
     */
    @PostMapping("/{storeId}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStoreStatus(@PathVariable Long storeId) {
        log.info("음식점 영업 상태 토글 요청: ID={}", storeId);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        StoreResponseDto responseDto = storeService.toggleStoreStatus(storeId, userEmail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "영업 상태가 변경되었습니다.");
        response.put("isActive", responseDto.getIsActive());
        response.put("status", responseDto.getIsActive() ? "영업중" : "휴업중");
        
        log.info("음식점 영업 상태 토글 완료: ID={}, 상태={}", storeId, responseDto.getIsActive());
        return ResponseEntity.ok(response);
    }
} 