package store.mealforyou.dto;

import lombok.*;
import store.mealforyou.constant.PaymentType;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class OrderPlaceDto {
    private List<Long> cartItemIds; // 선택된 장바구니 상품 ID들
    private PaymentType paymentType;
    private String receiverName;
    private String receiverPhone;
    private String address;
}