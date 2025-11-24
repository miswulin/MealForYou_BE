package store.mealforyou.dto;

import lombok.Data;
import store.mealforyou.constant.PaymentType;
import java.util.List;

@Data
public class PaymentRequestDto {
    // 포트원 결제 고유 번호
    private String impUid;

    // 주문 번호
    private String merchantUid;

    // 결제된 장바구니 상품 ID 목록
    private List<Long> cartItemIds;

    // 결제 수단
    private PaymentType paymentType;
}