package store.mealforyou.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartListDto {
    private List<CartItemDto> cartItems;
    private String totalProductPrice; // "38,500원"
    private String shippingFee;       // "2,500원"
    private String totalOrderPrice;   // "41,000원"
}