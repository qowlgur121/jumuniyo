package com.jumuniyo.controller.order;

import com.jumuniyo.dto.order.OrderResponseDto;
import com.jumuniyo.dto.order.OrderStatusUpdateRequestDto;
import com.jumuniyo.domain.order.OrderStatus;
import com.jumuniyo.security.CurrentUser;
import com.jumuniyo.security.UserPrincipal;
import com.jumuniyo.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "매장 주문 관리 API (사업자용)", description = "매장에서 받은 주문을 관리하는 API (사업자 인증 필요)")
@RestController
@RequestMapping("/api/v1/stores/{storeId}/orders")
@RequiredArgsConstructor
@Slf4j
public class StoreOrderController {
    
    private final OrderService orderService;
    
    @Operation(
        summary = "매장 주문 목록 조회",
        description = "특정 매장의 주문 목록을 조회합니다. 상태별 필터링 가능합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OrderResponseDto>> getStoreOrders(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "주문 상태 필터", required = false)
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(sort = "orderTime", direction = Sort.Direction.DESC) Pageable pageable) {
        
        log.info("매장 주문 목록 조회 요청: ownerId={}, storeId={}, status={}", 
                currentUser.getId(), storeId, status);
        
        // TODO: OrderService 메서드 구현 후 활성화
        /*
        Page<OrderResponseDto> responseDtos;
        if (status != null) {
            responseDtos = orderService.getStoreOrdersByStatus(storeId, status, pageable);
        } else {
            responseDtos = orderService.getStoreOrders(storeId, pageable);
        }
        
        return ResponseEntity.ok(responseDtos);
        */
        
        // 임시 응답
        return ResponseEntity.ok(Page.empty());
    }
    
    /**
     * 특정 기간 내 매장의 주문 목록 조회
     */
    @Operation(
        summary = "기간별 매장 주문 목록 조회",
        description = "지정된 기간 내의 매장 주문 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "기간별 주문 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "매장을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OrderResponseDto>> getStoreOrdersByDateRange(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "조회 시작 시간", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간", required = true, example = "2024-01-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        log.info("기간별 매장 주문 목록 조회 요청: ownerId={}, storeId={}, startTime={}, endTime={}", 
                currentUser.getId(), storeId, startTime, endTime);
        
        // TODO: OrderService 메서드 구현 후 활성화
        /*
        List<OrderResponseDto> responseDtos = orderService.getStoreOrdersByDateRange(
                storeId, startTime, endTime);
        
        return ResponseEntity.ok(responseDtos);
        */
        
        // 임시 응답
        return ResponseEntity.ok(List.of());
    }
    
    /**
     * 주문 상태 업데이트
     */
    @Operation(
        summary = "주문 상태 변경",
        description = "매장 주문의 상태를 변경합니다. (예: 접수, 준비중, 배달중, 완료 등)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "주문 ID", required = true, example = "123")
            @PathVariable Long orderId,
            @Parameter(description = "변경할 주문 상태 정보", required = true)
            @Valid @RequestBody OrderStatusUpdateRequestDto requestDto) {
        
        log.info("주문 상태 업데이트 요청: ownerId={}, storeId={}, orderId={}, newStatus={}", 
                currentUser.getId(), storeId, orderId, requestDto.getStatus());
        
        // TODO: OrderService 메서드 구현 후 활성화
        /*
        OrderResponseDto responseDto = orderService.updateOrderStatus(storeId, orderId, requestDto);
        
        return ResponseEntity.ok(responseDto);
        */
        
        // 임시 응답
        return ResponseEntity.ok(new OrderResponseDto());
    }
    
    /**
     * 주문 상세 조회 (사장님용)
     */
    @Operation(
        summary = "매장 주문 상세 조회",
        description = "특정 주문의 상세 정보를 조회합니다. (사업자 전용)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponseDto> getStoreOrder(
            @CurrentUser UserPrincipal currentUser,
            @Parameter(description = "매장 ID", required = true, example = "1")
            @PathVariable Long storeId,
            @Parameter(description = "주문 ID", required = true, example = "123")
            @PathVariable Long orderId) {
        
        log.info("매장 주문 상세 조회 요청: ownerId={}, storeId={}, orderId={}", 
                currentUser.getId(), storeId, orderId);
        
        OrderResponseDto responseDto = orderService.getOrderById(orderId);
        
        return ResponseEntity.ok(responseDto);
    }
} 