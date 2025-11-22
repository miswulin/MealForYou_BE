package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.constant.OrderStatus;
import store.mealforyou.dto.*;
import store.mealforyou.entity.*;
import store.mealforyou.repository.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final MemberRepository memberRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemIngredientRepository cartItemIngredientRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemIngredientRepository orderItemIngredientRepository;
    private final NumberFormat numberFormat = NumberFormat.getInstance(Locale.KOREA);

    // 주문서 조회 (선택된 상품만 포함)
    @Transactional(readOnly = true)
    public OrderSheetDto getOrderSheet(Long memberId, List<Long> cartItemIds) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        String addressStr = "배송지가 존재하지 않습니다.";
        if (member.getAddress() != null) {
            addressStr = member.getAddress().getRoadAddress() + " " + member.getAddress().getDetailAddress();
        }

        List<CartItem> selectedItems = cartItemRepository.findAllById(cartItemIds);
        List<OrderItemDto> orderItems = new ArrayList<>();
        int totalProductPriceInt = 0;

        for (CartItem item : selectedItems) {
            List<CartItemIngredient> options = cartItemIngredientRepository.findAllByCartItemId(item.getId());

            // 옵션 문자열 생성 (비어 있으면 빈 문자열)
            String optionStr = options.isEmpty() ? "" : options.stream()
                    .map(o -> o.getIngredient().getName() + "(" + o.getQuantity().intValue() + "개)")
                    .collect(Collectors.joining(", "));

            int itemTotal = item.getPrice() * item.getQuantity();

            orderItems.add(OrderItemDto.builder()
                    .dishName(item.getDish().getName())
                    .optionDescription(optionStr) // 깔끔하게 표시
                    .price(formatPrice(itemTotal))
                    .count(item.getQuantity() + "개")
                    .imageUrl(item.getDish().getMainDishImage() != null ? item.getDish().getMainDishImage().getPath() : "")
                    .build());

            totalProductPriceInt += itemTotal;
        }

        int shippingFeeInt = (totalProductPriceInt > 0) ? 2500 : 0;

        return OrderSheetDto.builder()
                .receiverName(member.getName())
                .receiverPhone(member.getPhoneE164())
                .address(addressStr)
                .orderItems(orderItems)
                .totalProductPrice(formatPrice(totalProductPriceInt))
                .shippingFee(formatPrice(shippingFeeInt))
                .finalTotalPrice(formatPrice(totalProductPriceInt + shippingFeeInt))
                .build();
    }

    // 주문 생성 (결제)
    public Long placeOrder(Long memberId, OrderPlaceDto request) {
        Member member = memberRepository.getReferenceById(memberId);
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

        // 총액 계산 (재료비 합산된 가격 * 수량)
        int totalAmount = cartItems.stream().mapToInt(c -> c.getPrice() * c.getQuantity()).sum();
        int shippingFee = (totalAmount > 0) ? 2500 : 0;

        // 주문 생성
        Order order = new Order();
        order.setMember(member);
        order.setDeliveryAddress(member.getAddress());
        order.setOrderNumber(RandomStringUtils.randomNumeric(12)); // 실무에선 UUID 권장
        order.setTotalAmount(totalAmount + shippingFee);
        order.setShippingFee(shippingFee);
        order.setPaymentType(request.getPaymentType());
        order.setOrderStatus(OrderStatus.ORDERED);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        // 장바구니 -> 주문 이관 작업
        for (CartItem ci : cartItems) {
            // OrderItem 생성
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setDish(ci.getDish());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getPrice());
            oi.setCreatedAt(LocalDateTime.now());
            orderItemRepository.save(oi);

            // CartItemIngredient 조회
            List<CartItemIngredient> ciiList = cartItemIngredientRepository.findAllByCartItemId(ci.getId());

            for (CartItemIngredient cii : ciiList) {
                // OrderItemIngredient로 복사
                OrderItemIngredient oii = new OrderItemIngredient();
                oii.setOrderItem(oi);
                oii.setIngredient(cii.getIngredient());
                oii.setMode(cii.getMode().name());
                oii.setQuantity(cii.getQuantity());
                oii.setFinalQuantity(cii.getFinalQuantity());
                orderItemIngredientRepository.save(oii);
            }

            // 자식 데이터(재료 옵션) 먼저 삭제
            cartItemIngredientRepository.deleteAll(ciiList);

            // 부모 데이터(장바구니 아이템) 삭제
            cartItemRepository.delete(ci);
        }
        return order.getId();
    }

    // 주문 상세
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        String fullDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String shortDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        String addressStr = "배송지가 존재하지 않습니다.";
        if (order.getDeliveryAddress() != null) {
            addressStr = order.getDeliveryAddress().getRoadAddress() + " "
                    + order.getDeliveryAddress().getDetailAddress();
        }

        List<OrderItemDto> items = new ArrayList<>();
        for(OrderItem oi : order.getOrderItems()) {
            List<OrderItemIngredient> opts = orderItemIngredientRepository.findAllByOrderItemId(oi.getId());

            // 옵션 문자열 생성 (비어 있으면 빈 문자열)
            String optionStr = opts.isEmpty() ? "" : opts.stream()
                    .map(o -> o.getIngredient().getName() + "(" + o.getQuantity().intValue() + "개)")
                    .collect(Collectors.joining(", "));

            items.add(OrderItemDto.builder()
                    .dishName(oi.getDish().getName())
                    .optionDescription(optionStr) // 깔끔하게 표시
                    .price(formatPrice(oi.getPrice() * oi.getQuantity()))
                    .count(oi.getQuantity() + "개")
                    .imageUrl(oi.getDish().getMainDishImage() != null ? oi.getDish().getMainDishImage().getPath() : "")
                    .build());
        }

        return OrderDetailDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(fullDate)
                .shortDate(shortDate)
                .status(order.getOrderStatus())
                .receiverName(order.getMember().getName())
                .receiverPhone(order.getMember().getPhoneE164())
                .address(addressStr)
                .items(items)
                .totalProductPrice(formatPrice(order.getTotalAmount() - order.getShippingFee()))
                .shippingFee(formatPrice(order.getShippingFee()))
                .totalAmount(formatPrice(order.getTotalAmount()))
                .build();
    }

    // 주문 내역 목록
    public List<OrderDetailDto> getOrderHistory(Long memberId) {
        return orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(order -> getOrderDetail(order.getId()))
                .collect(Collectors.toList());
    }

    private String formatPrice(int price) {
        return numberFormat.format(price) + "원";
    }
}