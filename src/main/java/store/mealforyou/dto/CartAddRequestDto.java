package store.mealforyou.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartAddRequestDto {
    private Long dishId;
    private List<IngredientOptionDto> options;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientOptionDto {
        private Long ingredientId;
        private int quantity;
    }
}