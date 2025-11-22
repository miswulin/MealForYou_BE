package store.mealforyou.dto;

import lombok.*;
import store.mealforyou.constant.OrderStatus;

import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Long orderId;
    private String orderNumber;
    private String orderDate;
    private String shortDate;
    private OrderStatus status;

    private String receiverName;
    private String receiverPhone;
    private String address;

    private List<OrderItemDto> items;

    private String totalProductPrice;
    private String shippingFee;
    private String totalAmount;
}
