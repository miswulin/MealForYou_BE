package store.mealforyou.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Long cartItemId;
    private Long dishId;
    private String dishName;
    private String imageUrl;

    private String optionDescription; // "마늘(10g) 2개, ..."

    private String quantity;    // "1개"
    private String totalPrice;  // "16,500원"

    private List<CartItemIngredientDto> ingredients;
}