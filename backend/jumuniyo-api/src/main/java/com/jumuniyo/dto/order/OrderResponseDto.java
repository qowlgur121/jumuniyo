package com.jumuniyo.dto.order;

import com.jumuniyo.domain.order.Order;
import com.jumuniyo.domain.order.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주문 응답 정보")
public class OrderResponseDto {

    @Schema(description = "주문 ID", example = "1")
    private Long id;

    @Schema(description = "주문 번호", example = "ORD20241226001")
    private String orderNumber;

    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String customerName;

    @Schema(description = "고객 전화번호", example = "010-1234-5678")
    private String customerPhone;

    @Schema(description = "매장 ID", example = "1")
    private Long storeId;

    @Schema(description = "매장 이름", example = "홍콩반점")
    private String storeName;

    @Schema(description = "배달 주소", example = "서울시 강남구 테헤란로 123")
    private String deliveryAddress;

    @Schema(description = "상세 배달 주소", example = "101동 1001호")
    private String deliveryAddressDetail;

    @Schema(description = "배달 요청사항", example = "문 앞에 놔주세요")
    private String deliveryRequest;

    @Schema(description = "가게 요청사항", example = "덜 맵게 해주세요")
    private String storeRequest;

    @Schema(description = "주문 상태")
    private OrderStatus status;

    @Schema(description = "주문 상태 설명", example = "조리 중")
    private String statusDescription;

    @Schema(description = "총 주문 금액", example = "25000")
    private BigDecimal totalAmount;

    @Schema(description = "취소 사유")
    private String cancelReason;

    @Schema(description = "주문 생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "주문 수정 시간")
    private LocalDateTime updatedAt;

    @Schema(description = "주문 항목 목록")
    private List<OrderItemResponseDto> orderItems;

    /**
     * 주문 상태 설명을 설정합니다.
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusDescription = status != null ? status.getDescription() : null;
    }

    /**
     * Order 엔티티를 OrderResponseDto로 변환
     */
    public static OrderResponseDto from(Order order) {
        if (order == null) {
            return null;
        }

        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        
        // 고객 정보
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getNickname());
        }
        dto.setCustomerPhone(order.getCustomerPhone());
        
        // 매장 정보
        if (order.getStore() != null) {
            dto.setStoreId(order.getStore().getId());
            dto.setStoreName(order.getStore().getName());
        }
        
        // 배달 정보
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setDeliveryAddressDetail(order.getDeliveryAddressDetail());
        dto.setDeliveryRequest(order.getDeliveryRequest());
        dto.setStoreRequest(order.getStoreRequest());
        
        // 주문 상태 및 금액
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCancelReason(order.getCancelReason());
        
        // 시간 정보
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // 주문 항목 변환
        if (order.getOrderItems() != null) {
            dto.setOrderItems(
                order.getOrderItems().stream()
                    .map(OrderItemResponseDto::from)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
} 