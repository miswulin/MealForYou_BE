package store.mealforyou.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.service.OrderService;
import java.util.List;

@RestController
@RequestMapping("/api/orders") // 공통 URL
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문서 (체크된 아이템만 조회)
    @GetMapping("/sheet")
    public ResponseEntity<OrderSheetDto> getOrderSheet(
            @RequestHeader("Member-Id") Long memberId,
            @RequestParam("items") List<Long> cartItemIds) {
        return ResponseEntity.ok(orderService.getOrderSheet(memberId, cartItemIds));
    }

    // 주문 생성 (결제)
    @PostMapping
    public ResponseEntity<Long> placeOrder(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody OrderPlaceDto request) {
        return ResponseEntity.ok(orderService.placeOrder(memberId, request));
    }

    // 주문 완료 페이지
    @GetMapping("/{orderId}/complete")
    public ResponseEntity<OrderDetailDto> getOrderComplete(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    // 주문 내역 목록
    @GetMapping("/history")
    public ResponseEntity<List<OrderDetailDto>> getOrderHistory(@RequestHeader("Member-Id") Long memberId) {
        return ResponseEntity.ok(orderService.getOrderHistory(memberId));
    }
}