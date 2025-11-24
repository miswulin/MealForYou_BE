package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.constant.ChangeMode;
import store.mealforyou.dto.*;
import store.mealforyou.entity.*;
import store.mealforyou.repository.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
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
    private final MemberRepository memberRepository;
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.KOREA);

    // 장바구니에 상품 추가 (장바구니 담기 / 바로 구매 공용)
    // 특징: 중복 메뉴 체크 없이 항상 새로운 행(Row)을 생성함
    public Long addItemToCart(Long memberId, CartAddRequestDto request) {
        // 옵션이 없으면 저장 불가
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw new IllegalArgumentException("옵션이 존재하지 않습니다.");
        }

        // 멤버 및 장바구니 조회 (없으면 생성)
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    newCart.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        // 메뉴(Dish) 조회
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));

        // CartItem 생성 (무조건 신규 생성)
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setDish(dish);
        cartItem.setQuantity(1);
        cartItem.setPrice(0); // 가격은 추후 계산 뒤 반영
        cartItem.setCreatedAt(LocalDateTime.now());

        cartItemRepository.save(cartItem); // ID 생성을 위해 먼저 저장

        // 재료 옵션 저장 및 가격 계산
        int singleProductPrice = 0;

        for (CartAddRequestDto.IngredientOptionDto optionDto : request.getOptions()) {
            Ingredient ingredient = ingredientRepository.findById(optionDto.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("재료가 존재하지 않습니다. ID=" + optionDto.getIngredientId()));

            CartItemIngredient cii = new CartItemIngredient();
            cii.setCartItem(cartItem);
            cii.setIngredient(ingredient);
            cii.setQuantity((double) optionDto.getQuantity());
            cii.setMode(ChangeMode.ABSOLUTE);
            cii.setFinalQuantity((double) optionDto.getQuantity());

            cartItemIngredientRepository.save(cii);

            // 가격 누적 (재료 단가 * 개수)
            singleProductPrice += ingredient.getUnitCost() * optionDto.getQuantity();
        }

        // 최종 가격 업데이트 (메뉴 1개당 가격)
        cartItem.setPrice(singleProductPrice);
        cartItemRepository.save(cartItem);

        return cartItem.getId();
    }

    // 장바구니 조회
    @Transactional(readOnly = true)
    public CartListDto getCartList(Long memberId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

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

            // 현재 상품 총 가격 = (DB에 저장된 단가) * (수량)
            int itemTotalPrice = item.getPrice() * item.getQuantity();

            itemDtos.add(CartItemDto.builder()
                    .cartItemId(item.getId())
                    .dishId(item.getDish().getId())
                    .dishName(item.getDish().getName())
                    .imageUrl(item.getDish().getMainDishImage() != null ? item.getDish().getMainDishImage().getPath() : "")
                    .optionDescription(makeOptionString(ciiList)) // 옵션 문자열 생성
                    .quantity(item.getQuantity() + "개")
                    .totalPrice(formatPrice(itemTotalPrice))
                    .ingredients(ingredientDtos)
                    .build());

            totalProductPriceInt += itemTotalPrice;
        }

        // 배송비 정책: 0원이면 0원, 아니면 2500원
        int shippingFeeInt = (totalProductPriceInt > 0) ? 2500 : 0;

        return CartListDto.builder()
                .cartItems(itemDtos)
                .totalProductPrice(formatPrice(totalProductPriceInt))
                .shippingFee(formatPrice(shippingFeeInt))
                .totalOrderPrice(formatPrice(totalProductPriceInt + shippingFeeInt))
                .build();
    }

    // 옵션 수량 변경 (장바구니 내부 수정)
    public CartListDto updateIngredientQuantity(Long memberId, CartItemIngredientUpdateDto dto) {
        CartItemIngredient cii = cartItemIngredientRepository.findById(dto.getCartItemIngredientId())
                .orElseThrow(() -> new IllegalArgumentException("옵션이 존재하지 않습니다."));

        // 수량 변경
        cii.setQuantity((double) dto.getQuantity());
        cartItemIngredientRepository.save(cii);

        // 가격 재계산 실행
        recalculateCartItemPrice(cii.getCartItem());

        return getCartList(memberId);
    }

    // 가격 재계산 로직 (재료 가격 합산)
    private void recalculateCartItemPrice(CartItem cartItem) {
        List<CartItemIngredient> options = cartItemIngredientRepository.findAllByCartItemId(cartItem.getId());

        int totalPrice = 0;
        for (CartItemIngredient cii : options) {
            totalPrice += (int) (cii.getIngredient().getUnitCost() * cii.getQuantity());
        }

        cartItem.setPrice(totalPrice);
        cartItemRepository.save(cartItem);
    }

    // 옵션 문자열 생성기
    private String makeOptionString(List<CartItemIngredient> list) {
        if (list.isEmpty()) return "";

        String joined = list.stream()
                .map(i -> i.getIngredient().getName() + " (" + i.getQuantity().intValue() + "개)")
                .collect(Collectors.joining(", "));

        // 25자 초과 시 생략
        if (joined.length() > 25) {
            joined = joined.substring(0, 25) + "···";
        }

        return joined;
    }

    private String formatPrice(int price) {
        return numberFormat.format(price) + "원";
    }

    // 상품 수량 변경
    public CartListDto updateCartItemQuantity(Long memberId, Long cartItemId, CartItemQuantityUpdateDto dto) {

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        int newQuantity;

        // 절대값 변경
        if (dto.getQuantity() != null) {
            newQuantity = dto.getQuantity();

            // 증감 변경
        } else if (dto.getDelta() != null) {
            newQuantity = item.getQuantity() + dto.getDelta();

        } else {
            throw new IllegalArgumentException("quantity 또는 delta 중 하나는 반드시 전달해야 합니다.");
        }

        // 0 이하일 시 삭제 처리
        if (newQuantity <= 0) {
            deleteCartItem(memberId, cartItemId);
            return getCartList(memberId);
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCartList(memberId);
    }

    // 상품 단일 삭제
    public void deleteCartItem(Long memberId, Long cartItemId) {
        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

        // 옵션 먼저 삭제
        List<CartItemIngredient> options = cartItemIngredientRepository.findAllByCartItemId(cartItemId);
        cartItemIngredientRepository.deleteAll(options);

        // 상품 삭제
        cartItemRepository.delete(item);
    }

    // 상품 다중 삭제
    public void deleteCartItems(Long memberId, List<Long> cartItemIds) {
        for (Long id : cartItemIds) {
            deleteCartItem(memberId, id);
        }
    }
}