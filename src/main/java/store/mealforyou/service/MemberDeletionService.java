package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.constant.OrderStatus;
import store.mealforyou.entity.CartItem;
import store.mealforyou.entity.CartItemIngredient;
import store.mealforyou.entity.Member;
import store.mealforyou.entity.Order;
import store.mealforyou.repository.*;

import java.util.List;

// 회원 탈퇴 시 연관된 데이터를 정리하는 전담 서비스
@Service
@RequiredArgsConstructor
public class MemberDeletionService {

    private final InterestRepository interestRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final CartItemIngredientRepository cartItemIngredientRepository;
    private final OrderRepository orderRepository;
    private final RefreshTokenRepository repository;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원과 연관된 데이터들을 정리한 뒤, 실제 회원을 삭제하는 메서드
    @Transactional
    public void deleteMemberWithRelations(Member member) {
        Long memberId = member.getId();
        String email = member.getEmail();

        // 관심 상품(Interest) 삭제
        // FK : interest.member_id -> members.member_id
        interestRepository.deleteByMember(member);

        // 장바구니 계열 (Cart, CartItem, CartItemIngredient) 정리
        // Cart ←(1:N)→ CartItem ←(1:N)→ CartItemIngredient
        // FK:
        // - carts.member_id → members.member_id
        // - cart_item.cart_id → carts.id
        // - cart_item_ingredient.cart_item_id → cart_item.id
        // 따라서 삭제 순서는 "가장 말단 자식 -> 부모" 순으로 진행함
        cartRepository.findByMemberId(memberId).ifPresent(cart -> {
            Long cartId = cart.getId();

            // 카트 아이템 조회
            List<CartItem> cartItems = cartItemRepository.findAllByCartId(cartId);

            // 각 CartItem에 속한 CartItemIngredient부터 삭제
            for (CartItem cartItem : cartItems) {
                Long cartItemId = cartItem.getId();
                List<CartItemIngredient> ingredients = cartItemIngredientRepository.findAllByCartItemId(cartItemId);
                cartItemIngredientRepository.deleteAll(ingredients);
            }

            // CartItem 삭제
            cartItemRepository.deleteAll(cartItems);

            // Cart 자체 삭제
            cartRepository.delete(cart);
        });

        // 주문 (Order) 논리 삭제 + 회원 참조 끊기
        // 주문 내역은 남기되 탈퇴 후에는 실제 회원과의 연결이 끊어져야 함
        List<Order> orders = orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
        for (Order order : orders) {
            // 논리적 삭제 상태로 마킹
            order.setOrderStatus(OrderStatus.DELETED);

            // 실제 FK 끊기 (회원 삭제 후에도 주문 레코드만 남도록)
            order.setMember(null);
        }
        // @Transactional 이므로 메서드 종료 시점에 변경 내용이 flush되어 업데이트됨

        // Redis Refresh Token 삭제
        refreshTokenRepository.delete(email);

        // Member 레코드 삭제
        memberRepository.delete(member);
    }
}
