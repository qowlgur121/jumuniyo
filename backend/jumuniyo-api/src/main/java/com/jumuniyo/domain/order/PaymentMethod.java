package com.jumuniyo.domain.order;

/**
 * 결제 방법을 나타내는 열거형
 */
public enum PaymentMethod {
    /**
     * 신용카드/체크카드
     */
    CARD("카드 결제"),
    
    /**
     * 계좌이체
     */
    BANK_TRANSFER("계좌이체"),
    
    /**
     * 휴대폰 결제
     */
    MOBILE("휴대폰 결제"),
    
    /**
     * 카카오페이
     */
    KAKAO_PAY("카카오페이"),
    
    /**
     * 네이버페이
     */
    NAVER_PAY("네이버페이"),
    
    /**
     * 토스페이
     */
    TOSS_PAY("토스페이"),
    
    /**
     * 현금 (직접 결제)
     */
    CASH("현금");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 