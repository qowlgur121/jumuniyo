package com.jumuniyo.repository.order;

import com.jumuniyo.domain.user.User;
import com.jumuniyo.domain.order.Order;
import com.jumuniyo.domain.order.OrderStatus;
import com.jumuniyo.domain.store.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 주문 Repository 인터페이스
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 주문 번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 고객 ID로 주문 목록 조회 (최신순)
     */
    Page<Order> findByCustomerOrderByCreatedAtDesc(User customer, Pageable pageable);

    /**
     * 매장 ID로 주문 목록 조회 (최신순)
     */
    Page<Order> findByStoreOrderByCreatedAtDesc(Store store, Pageable pageable);

    /**
     * 매장 ID와 상태로 주문 목록 조회 (최신순)
     */
    Page<Order> findByStoreAndStatusOrderByCreatedAtDesc(Store store, OrderStatus status, Pageable pageable);

    /**
     * 고객의 특정 기간 주문 내역 조회
     */
    List<Order> findByCustomerAndCreatedAtBetweenOrderByCreatedAtDesc(
            User customer, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * 매장의 특정 기간 주문 내역 조회
     */
    List<Order> findByStoreAndCreatedAtBetweenOrderByCreatedAtDesc(
            Store store, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * 매장의 활성 주문 목록 조회 (진행 중인 주문들)
     */
    @Query("SELECT o FROM Order o WHERE o.store = :store AND o.status IN ('PENDING', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY')")
    List<Order> findActiveOrdersByStore(@Param("store") Store store);

    /**
     * 특정 상태의 주문 수 조회
     */
    long countByStatus(OrderStatus status);

    /**
     * 매장의 특정 상태 주문 수 조회
     */
    long countByStoreAndStatus(Store store, OrderStatus status);

    /**
     * 고객의 최근 주문 조회
     */
    Optional<Order> findTopByCustomerOrderByCreatedAtDesc(User customer);

    /**
     * 특정 날짜 범위의 총 매출 조회
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 매장의 특정 날짜 범위 총 매출 조회
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.store = :store AND o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByStoreAndDateRange(
            @Param("store") Store store, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 고객의 취소된 주문 수 조회
     */
    long countByCustomerAndStatus(User customer, OrderStatus status);

    /**
     * 매장의 일일 주문 수 조회
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.store = :store AND DATE(o.createdAt) = DATE(:date)")
    long getDailyOrderCountByStore(@Param("store") Store store, @Param("date") LocalDateTime date);

    /**
     * 주문 상태별 통계 조회
     */
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.store = :store GROUP BY o.status")
    List<Object[]> getOrderStatsByStore(@Param("store") Store store);

    /**
     * 대기 중인 주문 목록 조회 (매장별)
     */
    List<Order> findByStoreAndStatusInOrderByCreatedAtAsc(Store store, List<OrderStatus> statuses);

    /**
     * 특정 고객과 매장의 주문 내역 조회
     */
    List<Order> findByCustomerAndStoreOrderByCreatedAtDesc(User customer, Store store);

    /**
     * 배달 지역별 주문 검색
     */
    @Query("SELECT o FROM Order o WHERE o.deliveryAddress LIKE %:address% AND o.status = :status")
    List<Order> findByDeliveryAddressContainingAndStatus(@Param("address") String address, @Param("status") OrderStatus status);

    /**
     * 최근 완료된 주문들 조회 (리뷰 요청용)
     */
    @Query("SELECT o FROM Order o WHERE o.customer = :customer AND o.status = 'COMPLETED' AND o.createdAt >= :date ORDER BY o.createdAt DESC")
    List<Order> findRecentCompletedOrdersForReview(
            @Param("customer") User customer, 
            @Param("date") LocalDateTime date
    );
} 