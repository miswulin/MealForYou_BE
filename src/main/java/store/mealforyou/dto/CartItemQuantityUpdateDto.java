package store.mealforyou.dto;

import lombok.*;

@Getter
public class CartItemQuantityUpdateDto {
    private Integer quantity; // 최종 수량
    private Integer delta; // 증감량
}