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

    private String optionDescription;

    private String quantity;
    private String totalPrice;

    private List<CartItemIngredientDto> ingredients;
}