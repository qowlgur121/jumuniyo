package com.jumuniyo.domain.order;

import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 주문 정보를 저장하는 엔티티
 * 요기요 클론 프로젝트의 핵심 주문 데이터를 관리합니다.
 */
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber; // 주문번호 (UUID 기반)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount; // 총 주문 금액

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryFee; // 배달비

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount; // 최종 결제 금액 (총 주문 금액 + 배달비)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.CREATED; // 주문 상태

    @Column(nullable = false, length = 255)
    private String deliveryAddress; // 배달 주소

    @Column(length = 255)
    private String deliveryAddressDetail; // 상세 배달 주소

    @Column(length = 500)
    private String deliveryRequest; // 배달 요청사항

    @Column(length = 500)
    private String storeRequest; // 가게 요청사항

    @Column(length = 20)
    private String customerPhone; // 주문자 전화번호

    @Column(length = 50)
    private String customerName; // 주문자 이름

    @Column
    private LocalDateTime expectedDeliveryTime; // 예상 배달 시간

    @Column
    private LocalDateTime actualDeliveryTime; // 실제 배달 완료 시간

    @Column(length = 500)
    private String cancelReason; // 취소 사유

    @Column
    private LocalDateTime canceledAt; // 취소 시간

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User customer; // 주문한 고객

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 주문받은 매장

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>(); // 주문 항목들

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 주문 생성 시간

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt; // 주문 수정 시간

    @Builder
    public Order(User customer, Store store, BigDecimal totalAmount, BigDecimal deliveryFee,
                 String deliveryAddress, String deliveryAddressDetail, String deliveryRequest,
                 String storeRequest, String customerPhone, String customerName,
                 LocalDateTime expectedDeliveryTime) {
        this.orderNumber = generateOrderNumber();
        this.customer = customer;
        this.store = store;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.finalAmount = totalAmount.add(deliveryFee);
        this.deliveryAddress = deliveryAddress;
        this.deliveryAddressDetail = deliveryAddressDetail;
        this.deliveryRequest = deliveryRequest;
        this.storeRequest = storeRequest;
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.expectedDeliveryTime = expectedDeliveryTime;
    }

    /**
     * 주문번호 생성 (현재 시간 + UUID 조합)
     */
    private String generateOrderNumber() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "ORD" + timestamp.substring(timestamp.length() - 8) + uuid.toUpperCase();
    }

    // 주문 상태 관리 메소드들
    public void markAsPaid() {
        if (this.status == OrderStatus.CREATED) {
            this.status = OrderStatus.PAID;
        } else {
            throw new IllegalStateException("결제는 생성된 주문에 대해서만 가능합니다.");
        }
    }

    public void acceptOrder() {
        if (this.status == OrderStatus.PAID) {
            this.status = OrderStatus.ACCEPTED;
        } else {
            throw new IllegalStateException("결제완료된 주문만 접수할 수 있습니다.");
        }
    }

    public void startCooking() {
        if (this.status == OrderStatus.ACCEPTED) {
            this.status = OrderStatus.COOKING;
        } else {
            throw new IllegalStateException("접수된 주문만 조리를 시작할 수 있습니다.");
        }
    }

    public void startDelivery() {
        if (this.status == OrderStatus.COOKING) {
            this.status = OrderStatus.DELIVERING;
        } else {
            throw new IllegalStateException("조리 중인 주문만 배달을 시작할 수 있습니다.");
        }
    }

    public void completeDelivery() {
        if (this.status != OrderStatus.DELIVERING) {
            throw new IllegalStateException("배달 중인 주문만 완료할 수 있습니다.");
        }
        this.status = OrderStatus.COMPLETED;
        this.actualDeliveryTime = LocalDateTime.now();
    }

    public void cancelOrder(String reason) {
        if (this.status.isCancelableByCustomer()) {
            this.status = OrderStatus.CANCELED;
            this.cancelReason = reason;
            this.canceledAt = LocalDateTime.now();
        } else {
            throw new IllegalStateException("현재 상태에서는 주문을 취소할 수 없습니다.");
        }
    }

    public void refundOrder() {
        if (this.status == OrderStatus.CANCELED) {
            this.status = OrderStatus.REFUNDED;
        } else {
            throw new IllegalStateException("취소된 주문만 환불 처리할 수 있습니다.");
        }
    }

    // 주문 정보 업데이트 메소드들
    public void updateDeliveryInfo(String deliveryAddress, String deliveryAddressDetail, 
                                   String deliveryRequest, String customerPhone) {
        if (this.status == OrderStatus.CREATED) {
            this.deliveryAddress = deliveryAddress;
            this.deliveryAddressDetail = deliveryAddressDetail;
            this.deliveryRequest = deliveryRequest;
            this.customerPhone = customerPhone;
        } else {
            throw new IllegalStateException("생성된 주문만 배달 정보를 수정할 수 있습니다.");
        }
    }

    public void updateExpectedDeliveryTime(LocalDateTime expectedDeliveryTime) {
        this.expectedDeliveryTime = expectedDeliveryTime;
    }

    // 연관관계 편의 메소드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        this.orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    // 주문 금액 재계산
    public void recalculateAmount() {
        this.totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.finalAmount = this.totalAmount.add(this.deliveryFee);
    }

    // 비즈니스 로직 메소드들
    public boolean isOwnedBy(User customer) {
        return this.customer.getId().equals(customer.getId());
    }

    // OrderServiceImpl에서 사용하는 메서드 별칭 추가
    public boolean isOwnedByCustomer(User customer) {
        return isOwnedBy(customer);
    }

    public boolean isManagedBy(User storeOwner) {
        return this.store.getOwner().getId().equals(storeOwner.getId());
    }
    
    // OrderServiceImpl에서 사용하는 메서드 별칭 추가
    public boolean isOwnedByStore(User storeOwner) {
        return isManagedBy(storeOwner);
    }

    public boolean canBeCanceledBy(User user) {
        return isOwnedBy(user) && this.status.isCancelableByCustomer();
    }

    public boolean canBeModifiedBy(User storeOwner) {
        return isManagedBy(storeOwner) && this.status.isManageableByStore();
    }
    
    // OrderServiceImpl에서 사용하는 cancel 메서드 별칭 추가
    public void cancel(String reason) {
        cancelOrder(reason);
    }

    public int getItemCount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    // 주문 소요 시간 계산 (분 단위)
    public Long getOrderDurationMinutes() {
        if (actualDeliveryTime != null) {
            return java.time.Duration.between(createdAt, actualDeliveryTime).toMinutes();
        }
        return null;
    }

    /**
     * 주문 상태 직접 업데이트 (관리자용)
     */
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("주문 상태는 null일 수 없습니다.");
        }
        this.status = newStatus;
    }
} 