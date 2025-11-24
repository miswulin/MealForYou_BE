package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.OrderService;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문 API", description = "주문/결제 페이지 조회, 결제 요청, 주문 생성, 주문 내역 조회 기능을 제공합니다.")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/sheet")
    @Operation(
            summary = "주문/결제 페이지 정보 조회",
            description = "선택한 상품으로 주문서 정보를 조회합니다. 배송지, 상품 리스트, 가격 정보 등을 반환합니다."
    )
    public ResponseEntity<OrderSheetDto> getOrderSheet(
            @AuthenticationPrincipal MemberDetails user,
            @RequestParam("items") List<Long> cartItemIds) {

        Long memberId = user.id();
        return ResponseEntity.ok(orderService.getOrderSheet(memberId, cartItemIds));
    }

    @PostMapping
    @Operation(
            summary = "주문 생성 (결제)",
            description = "주문/결제 페이지에서 결제 요청 시 호출합니다. 선택한 상품으로 주문을 생성합니다."
    )
    public ResponseEntity<Long> placeOrder(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody OrderPlaceDto request) {

        Long memberId = user.id();
        return ResponseEntity.ok(orderService.placeOrder(memberId, request));
    }

    @GetMapping("/{orderId}/complete")
    @Operation(
            summary = "주문 완료 페이지 조회",
            description = "주문 완료 후 상세 정보를 조회합니다."
    )
    public ResponseEntity<OrderDetailDto> getOrderComplete(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @GetMapping("/history")
    @Operation(
            summary = "주문 내역 조회",
            description = "로그인된 사용자의 모든 주문 내역을 조회합니다."
    )
    public ResponseEntity<List<OrderDetailDto>> getOrderHistory(
            @AuthenticationPrincipal MemberDetails user) {

        Long memberId = user.id();
        return ResponseEntity.ok(orderService.getOrderHistory(memberId));
    }
}