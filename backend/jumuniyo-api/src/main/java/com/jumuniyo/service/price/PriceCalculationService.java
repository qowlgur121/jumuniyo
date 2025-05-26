package com.jumuniyo.service.price;

import com.jumuniyo.domain.menu.Menu;
import com.jumuniyo.domain.menu.MenuOption;
import com.jumuniyo.dto.price.PriceBreakdownDto;

import java.math.BigDecimal;
import java.util.List;

public interface PriceCalculationService {

    /**
     * 메뉴와 선택된 옵션들을 기반으로 총 가격 계산
     */
    BigDecimal calculateTotalPrice(Menu menu, List<MenuOption> selectedOptions);

    /**
     * 가격 상세 내역 계산
     */
    PriceBreakdownDto calculatePriceBreakdown(Menu menu, List<MenuOption> selectedOptions);

    /**
     * 옵션들의 추가 가격 합계 계산
     */
    BigDecimal calculateOptionsPrice(List<MenuOption> selectedOptions);

    /**
     * 가격 포맷팅 (천 단위 구분자, 통화 기호 적용)
     */
    String formatPrice(BigDecimal price);

    /**
     * 할인 적용 가격 계산 (향후 확장용)
     */
    BigDecimal applyDiscount(BigDecimal originalPrice, BigDecimal discountRate);

    /**
     * 세금 포함 가격 계산 (향후 확장용)
     */
    BigDecimal calculatePriceWithTax(BigDecimal price, BigDecimal taxRate);
} 