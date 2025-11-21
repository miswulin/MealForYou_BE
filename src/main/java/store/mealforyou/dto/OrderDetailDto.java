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
    private String orderDate;      // "2025-10-11 17:40:30"
    private String shortDate;      // "25.10.11"
    private OrderStatus status;

    private String receiverName;
    private String receiverPhone;
    private String address;

    private List<OrderItemDto> items;

    private String totalProductPrice;
    private String shippingFee;
    private String totalAmount;
}
