package store.mealforyou.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class CartAddRequestDto {
    private Long dishId;
    private int quantity;
    private List<IngredientOptionDto> options;

    @Getter
    @NoArgsConstructor
    public static class IngredientOptionDto {
        private Long ingredientId;
        private int quantity;
    }
}