package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.dto.*;
import store.mealforyou.entity.*;
import store.mealforyou.repository.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemIngredientRepository cartItemIngredientRepository;
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.KOREA);

    // 1. 장바구니 조회
    @Transactional(readOnly = true)
    public CartListDto getCartList(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 없습니다."));

        List<CartItem> items = cartItemRepository.findAllByCartId(cart.getId());
        List<CartItemDto> itemDtos = new ArrayList<>();

        int totalProductPriceInt = 0;

        for (CartItem item : items) {
            // 옵션(재료) 조회
            List<CartItemIngredient> ciiList = cartItemIngredientRepository.findAllByCartItemId(item.getId());

            // 화면 표시용 재료 리스트
            List<CartItemIngredientDto> ingredientDtos = ciiList.stream()
                    .map(cii -> CartItemIngredientDto.builder()
                            .cartItemIngredientId(cii.getId())
                            .name(cii.getIngredient().getName())
                            .quantity(cii.getQuantity().intValue())
                            .build())
                    .collect(Collectors.toList());

            // 현재 아이템 총 가격 = (DB에 저장된 단가) * (수량)
            // ※ 단가는 아래 updateIngredientQuantity 호출 시 이미 계산되어 저장됨
            int itemTotalPrice = item.getPrice() * item.getQuantity();

            itemDtos.add(CartItemDto.builder()
                    .cartItemId(item.getId())
                    .dishId(item.getDish().getId()) // .getDish().getId()로 접근
                    .dishName(item.getDish().getName())
                    .imageUrl(item.getDish().getMainDishImage() != null ? item.getDish().getMainDishImage().getPath() : "")
                    .optionDescription(makeOptionString(ciiList))
                    .quantity(item.getQuantity() + "개")
                    .totalPrice(formatPrice(itemTotalPrice))
                    .ingredients(ingredientDtos)
                    .build());

            totalProductPriceInt += itemTotalPrice;
        }

        int shippingFeeInt = (totalProductPriceInt > 0) ? 2500 : 0;

        return CartListDto.builder()
                .cartItems(itemDtos)
                .totalProductPrice(formatPrice(totalProductPriceInt))
                .shippingFee(formatPrice(shippingFeeInt))
                .totalOrderPrice(formatPrice(totalProductPriceInt + shippingFeeInt))
                .build();
    }

    // 2. 옵션 수량 변경 (여기가 핵심!)
    public CartListDto updateIngredientQuantity(Long memberId, CartItemIngredientUpdateDto dto) {
        CartItemIngredient cii = cartItemIngredientRepository.findById(dto.getCartItemIngredientId())
                .orElseThrow(() -> new IllegalArgumentException("옵션이 존재하지 않습니다."));

        // 수량 변경
        cii.setQuantity((double) dto.getQuantity());
        cartItemIngredientRepository.save(cii);

        // ★ 가격 재계산 실행
        recalculateCartItemPrice(cii.getCartItem());

        return getCartList(memberId);
    }

    // ★ [중요] 가격 계산 로직 수정 완료 (재료비 합산 방식)
    private void recalculateCartItemPrice(CartItem cartItem) {
        // 기존 코드 삭제: int basePrice = cartItem.getDish().getBasePrice();

        // 해당 메뉴의 모든 재료 가져오기
        List<CartItemIngredient> options = cartItemIngredientRepository.findAllByCartItemId(cartItem.getId());

        int totalPrice = 0;
        for (CartItemIngredient cii : options) {
            // 재료 단가 * 재료 수량
            totalPrice += (int) (cii.getIngredient().getUnitCost() * cii.getQuantity());
        }

        // 합산된 가격을 CartItem의 가격으로 저장
        cartItem.setPrice(totalPrice);
        cartItemRepository.save(cartItem);
    }

    private String makeOptionString(List<CartItemIngredient> list) {
        if (list.isEmpty()) return "기본 구성";
        return list.stream()
                .map(i -> i.getIngredient().getName() + "(" + i.getQuantity().intValue() + "개)")
                .collect(Collectors.joining(", "));
    }

    private String formatPrice(int price) {
        return numberFormat.format(price) + "원";
    }
}