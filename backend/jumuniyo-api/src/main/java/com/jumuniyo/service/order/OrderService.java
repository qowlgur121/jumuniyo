package com.jumuniyo.service.order;

import com.jumuniyo.dto.order.OrderCreateRequestDto;
import com.jumuniyo.dto.order.OrderResponseDto;
import com.jumuniyo.dto.order.OrderStatusUpdateRequestDto;
import com.jumuniyo.domain.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 서비스 인터페이스
 * 주문 생성, 조회, 상태 관리 등의 비즈니스 로직을 정의합니다.
 */
public interface OrderService {
    
    /**
     * 새로운 주문 생성
     */
    OrderResponseDto createOrder(OrderCreateRequestDto requestDto, String customerEmail);
    
    /**
     * 주문 ID로 주문 조회
     */
    OrderResponseDto getOrderById(Long orderId);
    
    /**
     * 주문 번호로 주문 조회
     */
    OrderResponseDto getOrderByOrderNumber(String orderNumber);
    
    /**
     * 사용자의 주문 목록 조회
     */
    Page<OrderResponseDto> getOrdersByCustomer(String customerEmail, Pageable pageable);
    
    /**
     * 매장의 주문 목록 조회
     */
    Page<OrderResponseDto> getOrdersByStore(Long storeId, String ownerEmail, Pageable pageable);
    
    /**
     * 매장의 특정 상태 주문 목록 조회 (컨트롤러에서 사용)
     */
    Page<OrderResponseDto> getStoreOrdersByStatus(Long storeId, OrderStatus status, Pageable pageable, String ownerEmail);
    
    /**
     * 매장의 특정 상태 주문 목록 조회 (기존 메소드)
     */
    Page<OrderResponseDto> getOrdersByStoreAndStatus(Long storeId, OrderStatus status, String ownerEmail, Pageable pageable);
    
    /**
     * 주문 상태 업데이트
     */
    OrderResponseDto updateOrderStatus(Long orderId, Long storeId, OrderStatusUpdateRequestDto requestDto, String ownerEmail);
    
    /**
     * 주문 상태 변경 (컨트롤러에서 사용)
     */
    OrderResponseDto changeOrderStatus(Long orderId, OrderStatus targetStatus, String ownerEmail);
    
    /**
     * 주문 취소
     */
    OrderResponseDto cancelOrder(Long orderId, String customerEmail, String cancelReason);
    
    /**
     * 매장 관리자의 주문 상태 변경
     */
    OrderResponseDto processStoreOrderAction(Long orderId, Long storeId, OrderStatus targetStatus, String ownerEmail);
    
    /**
     * 특정 기간 내 사용자의 주문 목록 조회
     */
    List<OrderResponseDto> getOrdersByUserAndDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 특정 기간 내 매장의 주문 목록 조회
     */
    List<OrderResponseDto> getOrdersByStoreAndDateRange(Long storeId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 매장의 진행 중인 주문 조회
     */
    List<OrderResponseDto> getActiveOrdersByStore(Long storeId);
    
    /**
     * 주문 권한 확인
     */
    boolean hasCustomerPermission(Long orderId, String customerEmail);
    
    /**
     * 매장 권한 확인
     */
    boolean hasStorePermission(Long orderId, Long storeId, String ownerEmail);
}