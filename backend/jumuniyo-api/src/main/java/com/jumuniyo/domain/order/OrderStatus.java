package com.jumuniyo.domain.order;

/**
 * 주문 상태를 나타내는 열거형
 */
public enum OrderStatus {
    /**
     * 주문 생성됨 (결제 전)
     */
    CREATED("주문 생성됨"),
    
    /**
     * 결제 완료
     */
    PAID("결제 완료"),
    
    /**
     * 주문 대기 중
     */
    PENDING("주문 대기 중"),
    
    /**
     * 주문 확인됨 (매장 승인)
     */
    CONFIRMED("주문 확인됨"),
    
    /**
     * 주문 접수됨 (매장 접수)
     */
    ACCEPTED("주문 접수됨"),
    
    /**
     * 조리 중
     */
    PREPARING("조리 중"),
    
    /**
     * 조리 중 (별칭)
     */
    COOKING("조리 중"),
    
    /**
     * 픽업 준비 완료
     */
    READY_FOR_PICKUP("픽업 준비 완료"),
    
    /**
     * 배달 중
     */
    OUT_FOR_DELIVERY("배달 중"),
    
    /**
     * 배달 중 (별칭)
     */
    DELIVERING("배달 중"),
    
    /**
     * 배달 완료
     */
    DELIVERED("배달 완료"),
    
    /**
     * 주문 완료
     */
    COMPLETED("주문 완료"),
    
    /**
     * 주문 취소
     */
    CANCELLED("주문 취소"),
    
    /**
     * 주문 취소 (별칭)
     */
    CANCELED("주문 취소"),
    
    /**
     * 환불됨
     */
    REFUNDED("환불됨");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 고객이 취소 가능한 상태인지 확인
     */
    public boolean isCancelableByCustomer() {
        return this == CREATED || this == PAID || this == PENDING || this == CONFIRMED;
    }

    /**
     * 매장에서 관리 가능한 상태인지 확인
     */
    public boolean isManageableByStore() {
        return this == PAID || this == PENDING || this == CONFIRMED || 
               this == ACCEPTED || this == PREPARING || this == COOKING ||
               this == READY_FOR_PICKUP || this == OUT_FOR_DELIVERY || this == DELIVERING;
    }
} 