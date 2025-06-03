package com.jumuniyo.repository.order;

import com.jumuniyo.domain.order.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 주문 항목 Repository 인터페이스
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 메뉴 ID로 주문 항목 목록 조회
     */
    List<OrderItem> findByMenuId(Long menuId);

    /**
     * 매장별 인기 메뉴 조회
     */
    @Query("SELECT oi.menu.id, COUNT(oi) as count FROM OrderItem oi " +
           "WHERE oi.order.store.id = :storeId " +
           "GROUP BY oi.menu.id " +
           "ORDER BY count DESC")
    List<Object[]> findPopularMenusByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    /**
     * 주문 항목 개수 조회 (주문별)
     */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.order.id = :orderId")
    Long countByOrderId(@Param("orderId") Long orderId);

    /**
     * 메뉴의 총 주문 수량 조회
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.menu.id = :menuId")
    Long getTotalQuantityByMenuId(@Param("menuId") Long menuId);
} 