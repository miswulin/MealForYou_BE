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

    // 주문서 조회 (주문/결제 페이지 정보)
    public OrderSheetDto getOrderSheet(Long memberId, List<Long> cartItemIds) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        String addressStr = formatAddress(member.getAddress());
        String phoneStr = formatPhone(member.getPhoneE164());

        List<CartItem> selectedItems = cartItemRepository.findAllById(cartItemIds);
        List<OrderItemDto> orderItems = new ArrayList<>();
        int totalProductPriceInt = 0;

        for (CartItem item : selectedItems) {
            List<CartItemIngredient> options = cartItemIngredientRepository.findAllByCartItemId(item.getId());

            String optionStr = options.isEmpty() ? "" : options.stream()
                    .map(o -> o.getIngredient().getName() + "(" + o.getQuantity().intValue() + "개)")
                    .collect(Collectors.joining(", "));

            int itemTotal = item.getPrice() * item.getQuantity();

            orderItems.add(OrderItemDto.builder()
                    .dishName(item.getDish().getName())
                    .optionDescription(optionStr)
                    .price(formatPrice(itemTotal))
                    .count(item.getQuantity() + "개")
                    .imageUrl(item.getDish().getMainDishImage() != null ? item.getDish().getMainDishImage().getPath() : "")
                    .build());

            totalProductPriceInt += itemTotal;
        }

        int shippingFeeInt = (totalProductPriceInt > 0) ? 2500 : 0;

        return OrderSheetDto.builder()
                .receiverName(member.getName())
                .receiverPhone(phoneStr)
                .address(addressStr)
                .orderItems(orderItems)
                .totalProductPrice(formatPrice(totalProductPriceInt))
                .shippingFee(formatPrice(shippingFeeInt))
                .finalTotalPrice(formatPrice(totalProductPriceInt + shippingFeeInt))
                .build();
    }

    // 주문 생성 (주문/결제 페이지에서 결제하기 요청)
    public Long placeOrder(Long memberId, OrderPlaceDto request) {
        Member member = memberRepository.getReferenceById(memberId);
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

        int totalAmount = cartItems.stream().mapToInt(c -> c.getPrice() * c.getQuantity()).sum();
        int shippingFee = (totalAmount > 0) ? 2500 : 0;

        Order order = new Order();
        order.setMember(member);
        order.setDeliveryAddress(member.getAddress());
        order.setOrderNumber(RandomStringUtils.randomNumeric(12));
        order.setTotalAmount(totalAmount + shippingFee);
        order.setShippingFee(shippingFee);
        order.setPaymentType(request.getPaymentType());
        order.setOrderStatus(OrderStatus.ORDERED);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setDish(ci.getDish());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getPrice());
            oi.setCreatedAt(LocalDateTime.now());
            orderItemRepository.save(oi);

            List<CartItemIngredient> ciiList = cartItemIngredientRepository.findAllByCartItemId(ci.getId());
            for (CartItemIngredient cii : ciiList) {
                OrderItemIngredient oii = new OrderItemIngredient();
                oii.setOrderItem(oi);
                oii.setIngredient(cii.getIngredient());
                oii.setMode(cii.getMode().name());
                oii.setQuantity(cii.getQuantity());
                oii.setFinalQuantity(cii.getFinalQuantity());
                orderItemIngredientRepository.save(oii);
            }

            cartItemIngredientRepository.deleteAll(ciiList);
            cartItemRepository.delete(ci);
        }
        return order.getId();
    }

    // 주문 상세 조회
    public OrderDetailDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        String fullDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String shortDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yy.MM.dd"));

        String addressStr = formatAddress(order.getDeliveryAddress());
        String phoneStr = formatPhone(order.getMember().getPhoneE164());

        List<OrderItemDto> items = new ArrayList<>();
        for(OrderItem oi : order.getOrderItems()) {
            List<OrderItemIngredient> opts = orderItemIngredientRepository.findAllByOrderItemId(oi.getId());

            String optionStr = opts.isEmpty() ? "" : opts.stream()
                    .map(o -> o.getIngredient().getName() + "(" + o.getQuantity().intValue() + "개)")
                    .collect(Collectors.joining(", "));

            items.add(OrderItemDto.builder()
                    .dishName(oi.getDish().getName())
                    .optionDescription(optionStr)
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
                .receiverPhone(phoneStr)
                .address(addressStr)
                .items(items)
                .totalProductPrice(formatPrice(order.getTotalAmount() - order.getShippingFee()))
                .shippingFee(formatPrice(order.getShippingFee()))
                .totalAmount(formatPrice(order.getTotalAmount()))
                .build();
    }

    // 주문 내역 조회
    public List<OrderDetailDto> getOrderHistory(Long memberId) {
        return orderRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(order -> getOrderDetail(order.getId()))
                .collect(Collectors.toList());
    }

    private String formatPrice(int price) {
        return numberFormat.format(price) + "원";
    }

    private String formatPhone(String e164Phone) {
        if (e164Phone == null) return "";
        String raw = e164Phone.startsWith("+82") ? "0" + e164Phone.substring(3) : e164Phone;
        if (raw.length() != 11) return raw;
        return raw.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    private String formatAddress(Address address) {
        if (address == null) return "배송지가 존재하지 않습니다.";
        return String.format("(%s) %s, %s",
                address.getZipCode(),
                address.getRoadAddress(),
                address.getDetailAddress());
    }
}