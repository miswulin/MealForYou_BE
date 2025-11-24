package store.mealforyou.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
public class CartItemIngredientUpdateDto {
    private Long cartItemIngredientId;
    private int quantity;
}
