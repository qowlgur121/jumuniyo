package com.jumuniyo.controller.store;

import com.jumuniyo.dto.store.StoreCreateRequestDto;
import com.jumuniyo.dto.store.StoreResponseDto;
import com.jumuniyo.dto.store.StoreUpdateRequestDto;
import com.jumuniyo.service.store.StoreService;
import com.jumuniyo.service.file.FileUploadService;
import com.jumuniyo.util.PaginationUtils;
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
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "매장 관리 API (사업자용)", description = "매장 등록, 수정, 삭제, 조회 및 승인 관리 API (사업자 인증 필요)")
@RestController
@RequestMapping("/api/v1/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {

    private final StoreService storeService;
    private final FileUploadService fileUploadService;

    @Operation(
        summary = "매장 등록",
        description = "새로운 매장을 등록합니다. 인증된 사용자만 등록 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "매장 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping
    public ResponseEntity<StoreResponseDto> createStore(@Valid @RequestBody StoreCreateRequestDto requestDto) {
        log.info("음식점 등록 요청: {}", requestDto.getName());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        StoreResponseDto responseDto = storeService.createStore(requestDto, userEmail);
        
        log.info("음식점 등록 완료: ID={}, 이름={}", responseDto.getId(), responseDto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @Operation(
        summary = "매장 상세 조회",
        description = "특정 매장의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 조회 성공"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponseDto> getStore(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점 조회 요청: ID={}", storeId);
        
        StoreResponseDto responseDto = storeService.getStoreById(storeId);
        
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
        summary = "매장 정보 수정",
        description = "매장의 기본 정보를 수정합니다. 매장 소유자만 수정 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{storeId}")
    public ResponseEntity<StoreResponseDto> updateStore(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequestDto requestDto) {
        log.info("음식점 수정 요청: ID={}, 이름={}", storeId, requestDto.getName());
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        StoreResponseDto responseDto = storeService.updateStore(storeId, requestDto, userEmail);
        
        log.info("음식점 수정 완료: ID={}", storeId);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
        summary = "매장 삭제 (비활성화)",
        description = "매장을 삭제(비활성화) 처리합니다. 매장 소유자만 삭제 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}")
    public ResponseEntity<Map<String, String>> deleteStore(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점 삭제 요청: ID={}", storeId);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        storeService.deleteStore(storeId, userEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점이 성공적으로 삭제되었습니다.");
        
        log.info("음식점 삭제 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "내 매장 목록 조회",
        description = "로그인한 사용자가 소유한 매장 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/my")
    public ResponseEntity<Page<StoreResponseDto>> getMyStores(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("내 음식점 목록 조회 요청");
        
        // 페이지네이션 파라미터 검증
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        Page<StoreResponseDto> stores = storeService.getStoresByOwner(userEmail, validatedPageable);
        
        return ResponseEntity.ok(stores);
    }

    @Operation(
        summary = "매장 검색 및 목록 조회",
        description = "키워드, 카테고리, 지역 등의 조건으로 매장을 검색하고 다양한 필터를 적용하여 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public ResponseEntity<Page<StoreResponseDto>> searchStores(
            @Parameter(description = "검색 키워드", required = false)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 ID", required = false, example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "지역명", required = false)
            @RequestParam(required = false) String area,
            @Parameter(description = "정렬 기준 (rating, distance, review)", required = false)
            @RequestParam(required = false, defaultValue = "rating") String sortBy,
            @Parameter(description = "승인된 매장만 조회", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean approvedOnly,
            @Parameter(description = "최소 평점", required = false)
            @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "최대 최소주문금액", required = false)
            @RequestParam(required = false) BigDecimal maxMinimumOrderAmount,
            @Parameter(description = "최대 배달비", required = false)
            @RequestParam(required = false) BigDecimal maxDeliveryFee,
            @Parameter(description = "최대 배달시간(분)", required = false)
            @RequestParam(required = false) Integer maxDeliveryTime,
            @Parameter(description = "최소 리뷰 수", required = false)
            @RequestParam(required = false) Integer minReviewCount,
            @Parameter(description = "무료배달만", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean freeDeliveryOnly,
            @Parameter(description = "신규매장만", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean newStoreOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("음식점 검색 요청: keyword={}, categoryId={}, area={}, sortBy={}, minRating={}, maxDeliveryFee={}", 
                keyword, categoryId, area, sortBy, minRating, maxDeliveryFee);
        
        // 페이지네이션 파라미터 검증
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        
        Page<StoreResponseDto> stores = storeService.searchStoresWithFilters(
                keyword, categoryId, area, sortBy, approvedOnly, 
                minRating, maxMinimumOrderAmount, maxDeliveryFee, maxDeliveryTime, 
                minReviewCount, freeDeliveryOnly, newStoreOnly, validatedPageable);
        
        return ResponseEntity.ok(stores);
    }

    @Operation(
        summary = "위치 기반 매장 검색",
        description = "GPS 좌표를 기반으로 주변 매장을 검색하고 거리순 정렬 등 다양한 필터를 적용합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "위치 기반 매장 검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 좌표 정보"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/location")
    public ResponseEntity<Page<StoreResponseDto>> searchStoresWithLocation(
            @Parameter(description = "위도", required = true, example = "37.5665")
            @RequestParam BigDecimal latitude,
            @Parameter(description = "경도", required = true, example = "126.9780")
            @RequestParam BigDecimal longitude,
            @Parameter(description = "반경(km)", required = false, example = "5.0")
            @RequestParam(required = false) Double radiusKm,
            @Parameter(description = "검색 키워드", required = false)
            @RequestParam(required = false) String keyword,
            @Parameter(description = "카테고리 ID", required = false, example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "지역명", required = false)
            @RequestParam(required = false) String area,
            @Parameter(description = "정렬 기준 (distance, rating, review)", required = false)
            @RequestParam(required = false, defaultValue = "distance") String sortBy,
            @Parameter(description = "승인된 매장만 조회", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean approvedOnly,
            @Parameter(description = "최소 평점", required = false)
            @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "최대 최소주문금액", required = false)
            @RequestParam(required = false) BigDecimal maxMinimumOrderAmount,
            @Parameter(description = "최대 배달비", required = false)
            @RequestParam(required = false) BigDecimal maxDeliveryFee,
            @Parameter(description = "최대 배달시간(분)", required = false)
            @RequestParam(required = false) Integer maxDeliveryTime,
            @Parameter(description = "최소 리뷰 수", required = false)
            @RequestParam(required = false) Integer minReviewCount,
            @Parameter(description = "무료배달만", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean freeDeliveryOnly,
            @Parameter(description = "신규매장만", required = false)
            @RequestParam(required = false, defaultValue = "false") Boolean newStoreOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("위치 기반 음식점 검색 요청: lat={}, lng={}, radius={}, keyword={}, categoryId={}, area={}, sortBy={}", 
                latitude, longitude, radiusKm, keyword, categoryId, area, sortBy);
        
        // 페이지네이션 파라미터 검증
        Pageable validatedPageable = PaginationUtils.validateAndSanitizePageable(pageable);
        
        Page<StoreResponseDto> stores = storeService.searchStoresWithLocationAndFilters(
                keyword, categoryId, area, latitude, longitude, radiusKm, sortBy, approvedOnly,
                minRating, maxMinimumOrderAmount, maxDeliveryFee, maxDeliveryTime, 
                minReviewCount, freeDeliveryOnly, newStoreOnly, validatedPageable);
        
        return ResponseEntity.ok(stores);
    }

    @Operation(
        summary = "매장 승인 (관리자용)",
        description = "관리자가 매장 등록 신청을 승인 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 승인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/approve")
    public ResponseEntity<Map<String, String>> approveStore(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점 승인 요청: ID={}", storeId);
        
        storeService.approveStore(storeId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점이 승인되었습니다.");
        
        log.info("음식점 승인 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "매장 승인 거부 (관리자용)",
        description = "관리자가 매장 등록 신청을 거부 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "매장 승인 거부 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/reject")
    public ResponseEntity<Map<String, String>> rejectStore(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("음식점 승인 거부 요청: ID={}", storeId);
        
        storeService.rejectStore(storeId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "음식점 승인이 거부되었습니다.");
        
        log.info("음식점 승인 거부 완료: ID={}", storeId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "매장 영업 상태 토글",
        description = "매장의 영업 상태를 영업중/휴업중으로 토글합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "영업 상태 변경 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleStoreStatus(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
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

    @Operation(
        summary = "매장 로고 이미지 업로드",
        description = "매장의 로고 이미지를 업로드합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로고 이미지 업로드 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이미지 파일"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/{storeId}/logo")
    public ResponseEntity<Map<String, String>> uploadStoreLogo(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "로고 이미지 파일", required = true)
            @RequestPart("image") MultipartFile imageFile) {
        log.info("가게 로고 이미지 업로드 요청: storeId={}", storeId);
        
        // JWT에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        // 파일 유효성 검사
        if (!fileUploadService.isValidImageFile(imageFile)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "유효하지 않은 이미지 파일입니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 이미지 업로드
        String imageUrl = fileUploadService.uploadStoreLogoImage(imageFile, storeId);
        
        // 가게 정보 업데이트
        StoreResponseDto updatedStore = storeService.updateStoreLogo(storeId, imageUrl, userEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "로고 이미지가 성공적으로 업로드되었습니다.");
        response.put("imageUrl", imageUrl);
        
        log.info("가게 로고 이미지 업로드 완료: storeId={}, imageUrl={}", storeId, imageUrl);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "매장 로고 이미지 삭제",
        description = "매장의 로고 이미지를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로고 이미지 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{storeId}/logo")
    public ResponseEntity<Map<String, String>> deleteStoreLogo(
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId) {
        log.info("가게 로고 이미지 삭제 요청: storeId={}", storeId);
        
        // JWT에서 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        // 가게 로고 삭제
        storeService.deleteStoreLogo(storeId, userEmail);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "로고 이미지가 성공적으로 삭제되었습니다.");
        
        log.info("가게 로고 이미지 삭제 완료: storeId={}", storeId);
        return ResponseEntity.ok(response);
    }
} 