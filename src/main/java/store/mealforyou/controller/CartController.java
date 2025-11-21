package store.mealforyou.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.service.CartService;

@RestController
@RequestMapping("/api/cart") // 공통 URL 프리픽스 적용
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 조회
    // URL: GET /api/cart
    @GetMapping
    public ResponseEntity<CartListDto> getCart(@RequestHeader("Member-Id") Long memberId) {
        return ResponseEntity.ok(cartService.getCartList(memberId));
    }

    // 장바구니 재료 옵션 수량 변경
    // URL: PATCH /api/cart/ingredients
    @PatchMapping("/ingredients")
    public ResponseEntity<CartListDto> updateIngredient(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody CartItemIngredientUpdateDto request) {
        return ResponseEntity.ok(cartService.updateIngredientQuantity(memberId, request));
    }
}