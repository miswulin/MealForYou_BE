package store.mealforyou.dto;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemIngredientDto {
    private Long cartItemIngredientId;
    private String name;
    private int quantity;
}