package store.mealforyou.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSheetDto {
    private String receiverName;
    private String receiverPhone;
    private String address;

    private List<OrderItemDto> orderItems;

    private String totalProductPrice; // 선택된 상품만 합산
    private String shippingFee;
    private String finalTotalPrice;
}