package com.jumuniyo.service.price;

import com.jumuniyo.domain.menu.Menu;
import com.jumuniyo.domain.menu.MenuOption;
import com.jumuniyo.dto.price.PriceBreakdownDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PriceCalculationServiceImpl implements PriceCalculationService {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);

    @Override
    public BigDecimal calculateTotalPrice(Menu menu, List<MenuOption> selectedOptions) {
        log.debug("Calculating total price for menu: {}, options count: {}", menu.getId(), selectedOptions.size());

        BigDecimal basePrice = menu.getPrice();
        BigDecimal optionsPrice = calculateOptionsPrice(selectedOptions);
        BigDecimal totalPrice = basePrice.add(optionsPrice);

        log.debug("Price calculation - Base: {}, Options: {}, Total: {}", basePrice, optionsPrice, totalPrice);
        return totalPrice;
    }

    @Override
    public PriceBreakdownDto calculatePriceBreakdown(Menu menu, List<MenuOption> selectedOptions) {
        log.debug("Calculating price breakdown for menu: {}", menu.getId());

        BigDecimal basePrice = menu.getPrice();
        BigDecimal totalOptionsPrice = calculateOptionsPrice(selectedOptions);
        BigDecimal totalPrice = basePrice.add(totalOptionsPrice);

        // 옵션별 가격 정보 생성
        List<PriceBreakdownDto.OptionPriceDto> optionPrices = selectedOptions.stream()
                .map(option -> PriceBreakdownDto.OptionPriceDto.builder()
                        .optionId(option.getId())
                        .optionName(option.getName())
                        .additionalPrice(option.getAdditionalPrice())
                        .formattedPrice(formatPrice(option.getAdditionalPrice()))
                        .build())
                .collect(Collectors.toList());

        return PriceBreakdownDto.of(
                basePrice,
                optionPrices,
                totalOptionsPrice,
                totalPrice,
                formatPrice(totalPrice)
        );
    }

    @Override
    public BigDecimal calculateOptionsPrice(List<MenuOption> selectedOptions) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return selectedOptions.stream()
                .map(MenuOption::getAdditionalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0원";
        }

        // 소수점 제거 (원 단위로 반올림)
        BigDecimal roundedPrice = price.setScale(0, RoundingMode.HALF_UP);
        
        // 천 단위 구분자 적용
        String formattedNumber = CURRENCY_FORMAT.format(roundedPrice);
        
        return formattedNumber + "원";
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal originalPrice, BigDecimal discountRate) {
        if (originalPrice == null || discountRate == null) {
            return originalPrice != null ? originalPrice : BigDecimal.ZERO;
        }

        // 할인율이 0~1 사이인지 확인
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Invalid discount rate: {}. Must be between 0 and 1.", discountRate);
            return originalPrice;
        }

        BigDecimal discountAmount = originalPrice.multiply(discountRate);
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount);

        log.debug("Discount applied - Original: {}, Rate: {}, Discounted: {}", 
                originalPrice, discountRate, discountedPrice);

        return discountedPrice.max(BigDecimal.ZERO); // 음수 방지
    }

    @Override
    public BigDecimal calculatePriceWithTax(BigDecimal price, BigDecimal taxRate) {
        if (price == null || taxRate == null) {
            return price != null ? price : BigDecimal.ZERO;
        }

        // 세율이 음수가 아닌지 확인
        if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Invalid tax rate: {}. Must be non-negative.", taxRate);
            return price;
        }

        BigDecimal taxAmount = price.multiply(taxRate);
        BigDecimal priceWithTax = price.add(taxAmount);

        log.debug("Tax applied - Original: {}, Rate: {}, With Tax: {}", 
                price, taxRate, priceWithTax);

        return priceWithTax;
    }
} 