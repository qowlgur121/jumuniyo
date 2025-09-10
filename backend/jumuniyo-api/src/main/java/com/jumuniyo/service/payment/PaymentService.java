package com.jumuniyo.service.payment;

import com.jumuniyo.domain.order.Order;
import com.jumuniyo.domain.order.Payment;
import com.jumuniyo.domain.order.PaymentMethod;

/**
 * 결제 서비스 인터페이스
 * 결제 처리 및 관리 기능을 정의합니다.
 */
public interface PaymentService {
    
    /**
     * 결제 처리
     */
    Payment processPayment(Order order, PaymentMethod paymentMethod, String paymentKey);
    
    /**
     * 결제 조회
     */
    Payment getPaymentByOrder(Order order);
    
    /**
     * 결제 취소
     */
    void cancelPayment(Payment payment, String reason);
    
    /**
     * 환불 처리
     */
    void refundPayment(Payment payment, String reason);
} 