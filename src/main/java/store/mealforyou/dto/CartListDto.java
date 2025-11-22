package store.mealforyou.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartListDto {
    private List<CartItemDto> cartItems;
    private String totalProductPrice;
    private String shippingFee;
    private String totalOrderPrice;
}