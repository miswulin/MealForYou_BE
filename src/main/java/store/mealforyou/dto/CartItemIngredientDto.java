package store.mealforyou.dto;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemIngredientDto {
    private Long cartItemIngredientId;
    private String name;  // 재료명
    private int quantity; // 옵션 수량
}