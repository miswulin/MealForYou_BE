package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.service.CartService;

@RestController
@RequestMapping("/api/cart") // 공통 URL
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 담기
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody CartAddRequestDto request) {

        cartService.addItemToCart(memberId, request);
        return ResponseEntity.ok("장바구니에 상품이 담겼습니다.");
    }

    // 바로 구매
    @PostMapping("/buy")
    public ResponseEntity<Long> buyNow(
            @RequestHeader("Member-Id") Long memberId, // 👈 토큰 대신 헤더값 사용
            @RequestBody CartAddRequestDto request) {

        // 장바구니에 저장 로직 수행
        Long cartItemId = cartService.addItemToCart(memberId, request);

        // 생성된 아이템 ID 반환
        return ResponseEntity.ok(cartItemId);
    }

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<CartListDto> getCart(@RequestHeader("Member-Id") Long memberId) {
        return ResponseEntity.ok(cartService.getCartList(memberId));
    }

    // 장바구니 재료 옵션 수량 변경
    @PatchMapping("/ingredients")
    public ResponseEntity<CartListDto> updateIngredient(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody CartItemIngredientUpdateDto request) {
        return ResponseEntity.ok(cartService.updateIngredientQuantity(memberId, request));
    }
}