package com.jumuniyo.controller.order;

import com.jumuniyo.dto.order.OrderCreateRequestDto;
import com.jumuniyo.dto.order.OrderResponseDto;
import com.jumuniyo.dto.order.OrderStatusUpdateRequestDto;
import com.jumuniyo.domain.order.OrderStatus;
import com.jumuniyo.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 관련 REST API 컨트롤러
 * 주문 생성, 조회, 상태 관리 등의 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "주문 API", description = "주문 생성, 조회, 상태 관리 등 주문 처리 관련 API")
@SecurityRequirement(name = "bearerAuth") // 모든 API에 기본적으로 JWT 인증 필요 명시
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 새로운 주문 생성
     */
    @PostMapping
    @Operation(summary = "신규 주문 생성", description = "고객이 새로운 주문을 생성합니다. 인증된 사용자의 정보가 사용됩니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "주문 생성 요청 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderCreateRequestDto.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "주문 생성 성공", 
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효성 검사 실패)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto requestDto,
            @Parameter(hidden = true) Principal principal) {
        log.info("주문 생성 요청 - 고객: {}", principal.getName());
        
        OrderResponseDto response = orderService.createOrder(requestDto, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 주문 ID로 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "주문 ID로 상세 조회", description = "특정 주문 ID에 해당하는 주문의 상세 정보를 조회합니다.")
    @Parameter(name = "orderId", description = "조회할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 상세 정보 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 또는 관련 매장의 주문이 아님)"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        OrderResponseDto response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 번호로 주문 상세 조회
     */
    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "주문 번호로 상세 조회", description = "특정 주문 번호(고유 문자열)에 해당하는 주문의 상세 정보를 조회합니다.")
    @Parameter(name = "orderNumber", description = "조회할 주문의 번호", required = true, in = ParameterIn.PATH, example = "ORD20230101ABCDE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 상세 정보 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> getOrderByNumber(@PathVariable String orderNumber) {
        OrderResponseDto response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 고객의 주문 목록 조회
     */
    @GetMapping("/my")
    @Operation(summary = "나의 주문 목록 조회 (고객용)", description = "현재 인증된 고객의 주문 내역을 페이징 처리하여 조회합니다.")
    @Parameters({
        @Parameter(name = "pageable", description = "페이지 요청 정보 (size, page, sort 등)", 
                   content = @Content(schema = @Schema(implementation = Pageable.class)))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageOrderResponseDto.class))), // Page<OrderResponseDto> Wrapper 클래스 필요
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<Page<OrderResponseDto>> getMyOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            @Parameter(hidden = true) Pageable pageable, // Swagger UI에서 Pageable 파라미터는 Parameters 어노테이션으로 별도 관리
            @Parameter(hidden = true) Principal principal) {
        log.info("나의 주문 목록 조회 요청 - 고객: {}", principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        Page<OrderResponseDto> response = orderService.getOrdersByCustomer(principal.getName(), pageable);
        // Page<OrderResponseDto> response = Page.empty(pageable); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 취소
     */
    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "주문 취소 (고객용)", description = "고객이 자신의 주문을 취소합니다. 특정 주문 상태에서만 가능합니다.")
    @Parameters({
        @Parameter(name = "orderId", description = "취소할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1"),
        @Parameter(name = "cancelReason", description = "주문 취소 사유", required = true, in = ParameterIn.QUERY, example = "단순 변심")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 취소 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 취소 불가능한 상태)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 주문이 아님)"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam String cancelReason,
            @Parameter(hidden = true) Principal principal) {
        log.info("주문 취소 요청 - 주문 ID: {}, 고객: {}, 사유: {}", orderId, principal.getName(), cancelReason);
        // TODO: OrderService 메서드 구현 후 활성화
        OrderResponseDto response = orderService.cancelOrder(orderId, principal.getName(), cancelReason);
        // OrderResponseDto response = new OrderResponseDto(); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 매장의 주문 목록 조회
     */
    @GetMapping("/store/{storeId}")
    @Operation(summary = "매장의 주문 목록 조회 (사업자용)", description = "특정 매장의 주문 내역을 페이징 및 상태별 필터링하여 조회합니다. 인증된 사업자만 접근 가능합니다.")
    @Parameters({
        @Parameter(name = "storeId", description = "주문 목록을 조회할 매장의 ID", required = true, in = ParameterIn.PATH, example = "1"),
        @Parameter(name = "status", description = "조회할 주문 상태 (예: PENDING, COOKING, DELIVERED). 지정하지 않으면 전체 상태 조회.", 
                   in = ParameterIn.QUERY, schema = @Schema(implementation = OrderStatus.class)),
        @Parameter(name = "pageable", description = "페이지 요청 정보", 
                   content = @Content(schema = @Schema(implementation = Pageable.class)))
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매장 주문 목록 조회 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageOrderResponseDto.class))), // Page<OrderResponseDto> Wrapper 클래스 필요
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (해당 매장 사업자가 아님)")
    })
    public ResponseEntity<Page<OrderResponseDto>> getStoreOrders(
            @PathVariable Long storeId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) 
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) Principal principal) {
        log.info("매장 주문 목록 조회 요청 - 매장 ID: {}, 상태: {}, 사업자: {}", storeId, status, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        Page<OrderResponseDto> response;
        if (status != null) {
            response = orderService.getStoreOrdersByStatus(storeId, status, pageable, principal.getName());
        } else {
            response = orderService.getOrdersByStore(storeId, principal.getName(), pageable);
        }
        // Page<OrderResponseDto> response = Page.empty(pageable); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 상태 업데이트 (매장용)
     */
    @PatchMapping("/store/{storeId}/{orderId}/status") // 경로 수정: /store/{storeId}/orders/{orderId}/status 와 같이 명확하게 하는 것도 고려
    @Operation(summary = "주문 상태 업데이트 (사업자용)", description = "매장에서 특정 주문의 상태를 업데이트합니다 (예: 조리중 -> 배달시작).")
    @Parameters({
        @Parameter(name = "storeId", description = "매장 ID", required = true, in = ParameterIn.PATH, example = "1"),
        @Parameter(name = "orderId", description = "상태를 업데이트할 주문 ID", required = true, in = ParameterIn.PATH, example = "1")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "업데이트할 주문 상태 정보",
            required = true,
            content = @Content(schema = @Schema(implementation = OrderStatusUpdateRequestDto.class))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 상태 업데이트 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않은 상태 변경)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문 또는 매장을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long storeId,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto requestDto,
            @Parameter(hidden = true) Principal principal) {
        log.info("주문 상태 업데이트 요청 - 매장 ID: {}, 주문 ID: {}, 상태: {}, 사업자: {}", storeId, orderId, requestDto.getStatus(), principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        OrderResponseDto response = orderService.updateOrderStatus(storeId, orderId, requestDto, principal.getName());
        // OrderResponseDto response = new OrderResponseDto(); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    // 아래 API들은 updateOrderStatus로 통합하거나, 더 세분화된 역할이 있다면 유지합니다.
    // 여기서는 각 상태 변경을 명시적인 API로 분리했다고 가정하고 어노테이션을 추가합니다.

    /**
     * 주문 접수 (매장용)
     */
    @PatchMapping("/{orderId}/accept")
    @Operation(summary = "주문 접수 (사업자용)", description = "매장에서 고객의 주문을 접수 처리합니다.")
    @Parameter(name = "orderId", description = "접수할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 접수 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 처리된 주문이거나 접수 불가능한 상태"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> acceptOrder(
            @PathVariable Long orderId,
            @Parameter(hidden = true) Principal principal) {
        log.info("주문 접수 요청 - 주문 ID: {}, 사업자: {}", orderId, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        OrderResponseDto response = orderService.changeOrderStatus(orderId, OrderStatus.COOKING, principal.getName()); // 예시: OrderService에 상태 변경 로직 위임
        // OrderResponseDto response = new OrderResponseDto(); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 조리 시작 (매장용)
     */
    @PatchMapping("/{orderId}/start-cooking")
    @Operation(summary = "조리 시작 (사업자용)", description = "매장에서 주문된 음식의 조리를 시작합니다.")
    @Parameter(name = "orderId", description = "조리를 시작할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조리 시작 처리 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 상태"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> startCooking(
            @PathVariable Long orderId,
            @Parameter(hidden = true) Principal principal) {
        log.info("조리 시작 요청 - 주문 ID: {}, 사업자: {}", orderId, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        OrderResponseDto response = orderService.changeOrderStatus(orderId, OrderStatus.COOKING, principal.getName()); // COOKING으로 변경하는 예시, 실제로는 PREPARING 등일 수 있음
        // OrderResponseDto response = new OrderResponseDto(); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 배달 시작 (매장용)
     */
    @PatchMapping("/{orderId}/start-delivery")
    @Operation(summary = "배달 시작 (사업자용)", description = "매장에서 조리 완료된 음식의 배달을 시작합니다.")
    @Parameter(name = "orderId", description = "배달을 시작할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배달 시작 처리 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 상태"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> startDelivery(
            @PathVariable Long orderId,
            @Parameter(hidden = true) Principal principal) {
        log.info("배달 시작 요청 - 주문 ID: {}, 사업자: {}", orderId, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        OrderResponseDto response = orderService.changeOrderStatus(orderId, OrderStatus.DELIVERING, principal.getName());
        // OrderResponseDto response = new OrderResponseDto(); // 임시 코드
        return ResponseEntity.ok(response);
    }
    
    /**
     * 배달 완료 (매장용)
     */
    @PatchMapping("/{orderId}/delivered")
    @Operation(summary = "배달 완료 처리", description = "주문의 배달을 완료 처리합니다.")
    @Parameters({
            @Parameter(name = "orderId", description = "배달 완료할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배달 완료 처리 성공",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 배달 완료된 주문 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<OrderResponseDto> markAsDelivered(
            @PathVariable Long orderId,
            Principal principal) {
        
        OrderResponseDto response = orderService.changeOrderStatus(orderId, OrderStatus.COMPLETED, principal.getName());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 주문 권한 확인 (고객)
     */
    @GetMapping("/{orderId}/permission/customer")
    @Operation(summary = "주문 권한 확인 (고객용)", description = "현재 인증된 고객이 특정 주문에 대한 조회/수정 권한이 있는지 확인합니다.")
    @Parameter(name = "orderId", description = "권한을 확인할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 확인 결과 (true/false)",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<Boolean> checkCustomerPermission(
            @PathVariable Long orderId,
            @Parameter(hidden = true) Principal principal) {
        log.info("고객 주문 권한 확인 요청 - 주문 ID: {}, 고객: {}", orderId, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        boolean hasPermission = orderService.hasCustomerPermission(orderId, principal.getName());
        // boolean hasPermission = false; // 임시 코드
        return ResponseEntity.ok(hasPermission);
    }
    
    /**
     * 주문 권한 확인 (매장)
     */
    @GetMapping("/{orderId}/permission/store")
    @Operation(summary = "주문 권한 확인 (사업자용)", description = "현재 인증된 사업자가 특정 주문에 대한 관리 권한이 있는지 확인합니다.")
    @Parameter(name = "orderId", description = "권한을 확인할 주문의 ID", required = true, in = ParameterIn.PATH, example = "1")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 확인 결과 (true/false)",
                         content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public ResponseEntity<Boolean> checkStorePermission(
            @PathVariable Long orderId,
            @Parameter(hidden = true) Principal principal) {
        log.info("사업자 주문 권한 확인 요청 - 주문 ID: {}, 사업자: {}", orderId, principal.getName());
        // TODO: OrderService 메서드 구현 후 활성화
        boolean hasPermission = orderService.hasCustomerPermission(orderId, principal.getName()); // 임시로 hasCustomerPermission 사용
        // boolean hasPermission = false; // 임시 코드
        return ResponseEntity.ok(hasPermission);
    }

    // Page<OrderResponseDto>를 위한 Wrapper 클래스 (Swagger 문서용)
    // 실제 코드에서는 Page 인터페이스가 제네릭을 포함하므로 Swagger에서 정확히 표현하기 어려울 수 있음.
    // 이런 경우, 아래와 같이 구체적인 타입을 명시한 클래스를 응답 스키마로 사용.
    @Schema(name = "PageOrderResponseDto")
    private static class PageOrderResponseDto extends PageImpl<OrderResponseDto> {
        public PageOrderResponseDto(List<OrderResponseDto> content, Pageable pageable, long total) {
            super(content, pageable, total);
        }
        // 필요시 추가적인 getter나 생성자 정의 가능
    }
} 