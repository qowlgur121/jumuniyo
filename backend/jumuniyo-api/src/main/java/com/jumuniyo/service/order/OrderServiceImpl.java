package com.jumuniyo.service.order;

import com.jumuniyo.domain.menu.Menu;
import com.jumuniyo.domain.menu.MenuOption;
import com.jumuniyo.domain.order.*;
import com.jumuniyo.domain.store.Store;
import com.jumuniyo.domain.user.User;
import com.jumuniyo.dto.order.OrderCreateRequestDto;
import com.jumuniyo.dto.order.OrderResponseDto;
import com.jumuniyo.dto.order.OrderStatusUpdateRequestDto;
import com.jumuniyo.repository.order.OrderRepository;
import com.jumuniyo.repository.order.OrderItemRepository;
import com.jumuniyo.repository.menu.MenuOptionRepository;
import com.jumuniyo.repository.menu.MenuRepository;
import com.jumuniyo.repository.store.StoreRepository;
import com.jumuniyo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 서비스 구현체
 * 주문 생성, 조회, 상태 관리 등의 비즈니스 로직을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Transactional
    @Override
    public OrderResponseDto createOrder(OrderCreateRequestDto requestDto, String customerEmail) {
        log.info("새로운 주문 생성 시작 - 고객: {}, 매장 ID: {}", customerEmail, requestDto.getStoreId());

        // 고객 조회
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        // 매장 조회
        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));

        // 주문 생성
        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .totalAmount(requestDto.getTotalAmount())
                .deliveryFee(requestDto.getDeliveryFee())
                .deliveryAddress(requestDto.getDeliveryAddress())
                .deliveryAddressDetail(requestDto.getDeliveryAddressDetail())
                .deliveryRequest(requestDto.getDeliveryRequest())
                .storeRequest(requestDto.getStoreRequest())
                .customerPhone(requestDto.getPhoneNumber())
                .customerName(requestDto.getCustomerName())
                .expectedDeliveryTime(LocalDateTime.now().plusMinutes(30)) // 기본 30분 후
                .build();

        // 주문 항목 생성
        for (OrderCreateRequestDto.OrderItemDto itemDto : requestDto.getOrderItems()) {
            Menu menu = menuRepository.findById(itemDto.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + itemDto.getMenuId()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menu(menu)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(menu.getPrice())
                    .build();

            // 옵션 처리
            if (itemDto.getOptions() != null) {
                for (OrderCreateRequestDto.OrderItemDto.OrderItemOptionDto optionDto : itemDto.getOptions()) {
                    MenuOption menuOption = menuOptionRepository.findById(optionDto.getMenuOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("메뉴 옵션을 찾을 수 없습니다: " + optionDto.getMenuOptionId()));

                    OrderItemOption orderItemOption = OrderItemOption.builder()
                            .orderItem(orderItem)
                            .optionGroupName(menuOption.getOptionGroup().getName())
                            .optionName(menuOption.getName())
                            .optionPrice(menuOption.getAdditionalPrice())
                            .build();

                    orderItem.addOrderItemOption(orderItemOption);
                }
            }

            order.addOrderItem(orderItem);
        }

        // 주문 저장
        Order savedOrder = orderRepository.save(order);
        
        log.info("주문 생성 완료 - 주문번호: {}", savedOrder.getOrderNumber());
        
        return OrderResponseDto.from(savedOrder);
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        
        return OrderResponseDto.from(order);
    }

    @Override
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderNumber));
        
        return OrderResponseDto.from(order);
    }

    @Override
    public Page<OrderResponseDto> getOrdersByCustomer(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerEmail));
        
        Page<Order> orders = orderRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
        return orders.map(OrderResponseDto::from);
    }

    @Override
    public Page<OrderResponseDto> getOrdersByStore(Long storeId, String ownerEmail, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 권한 확인
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        if (!store.isOwnedBy(owner)) {
            throw new IllegalArgumentException("해당 매장에 접근할 권한이 없습니다.");
        }
        
        Page<Order> orders = orderRepository.findByStoreOrderByCreatedAtDesc(store, pageable);
        return orders.map(OrderResponseDto::from);
    }

    @Override
    public Page<OrderResponseDto> getStoreOrdersByStatus(Long storeId, OrderStatus status, Pageable pageable, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        // 권한 확인
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        if (!store.isOwnedBy(owner)) {
            throw new IllegalArgumentException("해당 매장에 접근할 권한이 없습니다.");
        }
        
        Page<Order> orders = orderRepository.findByStoreAndStatusOrderByCreatedAtDesc(store, status, pageable);
        return orders.map(OrderResponseDto::from);
    }

    @Override
    public Page<OrderResponseDto> getOrdersByStoreAndStatus(Long storeId, OrderStatus status, String ownerEmail, Pageable pageable) {
        return getStoreOrdersByStatus(storeId, status, pageable, ownerEmail);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, Long storeId, OrderStatusUpdateRequestDto requestDto, String ownerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));

        if (!order.isOwnedByStore(owner)) {
            throw new IllegalArgumentException("주문 상태를 변경할 권한이 없습니다.");
        }

        // 상태 업데이트
        order.updateStatus(requestDto.getStatus());
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("주문 상태 변경 - 주문번호: {}, 변경된 상태: {}", order.getOrderNumber(), requestDto.getStatus());
        
        return OrderResponseDto.from(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto changeOrderStatus(Long orderId, OrderStatus targetStatus, String ownerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));

        if (!order.isOwnedByStore(owner)) {
            throw new IllegalArgumentException("주문 상태를 변경할 권한이 없습니다.");
        }

        // 상태 업데이트
        order.updateStatus(targetStatus);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("주문 상태 변경 - 주문번호: {}, 변경된 상태: {}", order.getOrderNumber(), targetStatus);
        
        return OrderResponseDto.from(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId, String customerEmail, String cancelReason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 권한 확인
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + customerEmail));

        if (!order.isOwnedByCustomer(customer)) {
            throw new IllegalArgumentException("주문을 취소할 권한이 없습니다.");
        }

        // 취소 가능 여부 확인
        if (!order.getStatus().isCancelableByCustomer()) {
            throw new IllegalArgumentException("현재 상태에서는 주문을 취소할 수 없습니다.");
        }

        // 주문 취소
        order.cancel(cancelReason);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("주문 취소 완료 - 주문번호: {}, 취소사유: {}", order.getOrderNumber(), cancelReason);
        
        return OrderResponseDto.from(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto processStoreOrderAction(Long orderId, Long storeId, OrderStatus targetStatus, String ownerEmail) {
        return changeOrderStatus(orderId, targetStatus, ownerEmail);
    }

    @Override
    public List<OrderResponseDto> getOrdersByUserAndDateRange(String userEmail, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userEmail));
        
        List<Order> orders = orderRepository.findByCustomerAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDate, endDate);
        return orders.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getOrdersByStoreAndDateRange(Long storeId, LocalDateTime startDate, LocalDateTime endDate) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        List<Order> orders = orderRepository.findByStoreAndCreatedAtBetweenOrderByCreatedAtDesc(store, startDate, endDate);
        return orders.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponseDto> getActiveOrdersByStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다: " + storeId));
        
        List<Order> orders = orderRepository.findActiveOrdersByStore(store);
        return orders.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasCustomerPermission(Long orderId, String customerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerEmail));
        
        return order.isOwnedByCustomer(customer);
    }

    @Override
    public boolean hasStorePermission(Long orderId, Long storeId, String ownerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));
        
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + ownerEmail));
        
        return order.isOwnedByStore(owner) && order.getStore().getId().equals(storeId);
    }
} 