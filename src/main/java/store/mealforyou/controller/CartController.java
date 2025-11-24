package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.CartService;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "장바구니 API", description = "상품 등록, 조회, 수정 기능을 제공합니다.")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(
            summary = "장바구니 등록",
            description = "상품을 장바구니에 등록합니다."
    )
    public ResponseEntity<String> addToCart(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody CartAddRequestDto request) {

        cartService.addItemToCart(user.id(), request);
        return ResponseEntity.ok("상품이 장바구니에 등록되었습니다.");
    }

    @PostMapping("/buy")
    @Operation(
            summary = "바로 구매",
            description = "상품을 장바구니에 등록한 뒤 생성된 cartItemId를 반환합니다. 주문/결제 페이지로 이동 시에 사용합니다."
    )
    public ResponseEntity<Long> buyNow(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody CartAddRequestDto request) {

        Long cartItemId = cartService.addItemToCart(user.id(), request);
        return ResponseEntity.ok(cartItemId);
    }

    @GetMapping
    @Operation(
            summary = "장바구니 조회",
            description = "로그인된 사용자의 장바구니 목록을 조회합니다."
    )
    public ResponseEntity<CartListDto> getCart(
            @AuthenticationPrincipal MemberDetails user) {

        return ResponseEntity.ok(cartService.getCartList(user.id()));
    }

    @PatchMapping("/ingredients")
    @Operation(
            summary = "옵션 수량 변경",
            description = "장바구니에 담긴 상품의 옵션 수량을 변경합니다."
    )
    public ResponseEntity<CartListDto> updateIngredient(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody CartItemIngredientUpdateDto request) {

        return ResponseEntity.ok(cartService.updateIngredientQuantity(user.id(), request));
    }

    @PatchMapping("{cartItemId}/quantity")
    @Operation(
            summary = "상품 수량 변경",
            description = """
                상품의 수량을 변경합니다.
                
                - quantity: 최종 수량 (절대값 변경)
                - delta: +1 / -1 등 증감 변경
                - quantity 또는 delta 중 하나는 반드시 전달
                - 변경 결과가 0 이하이면 해당 상품은 자동 삭제됩니다.
                """
    )
    public ResponseEntity<CartListDto> updateCartItemQuantity(
            @AuthenticationPrincipal MemberDetails user,
            @PathVariable Long cartItemId,
            @RequestBody CartItemQuantityUpdateDto dto
    ) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(user.id(), cartItemId, dto));
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(
            summary = "상품 단일 삭제",
            description = "장바구니에서 상품을 삭제 후, 목록을 최신화하여 반환합니다."
    )
    public ResponseEntity<CartListDto> deleteItem(
            @AuthenticationPrincipal MemberDetails user,
            @PathVariable Long cartItemId) {

        cartService.deleteCartItem(user.id(), cartItemId);
        return ResponseEntity.ok(cartService.getCartList(user.id()));
    }

    @DeleteMapping("/items")
    @Operation(
            summary = "상품 다중 삭제",
            description = "장바구니에서 상품을 삭제 후, 목록을 최신화하여 반환합니다."
    )
    public ResponseEntity<CartListDto> deleteItems(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody List<Long> cartItemIds) {

        cartService.deleteCartItems(user.id(), cartItemIds);
        return ResponseEntity.ok(cartService.getCartList(user.id()));
    }
}