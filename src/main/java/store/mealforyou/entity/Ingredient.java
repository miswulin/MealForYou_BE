package store.mealforyou.entity;

import store.mealforyou.constant.IngredientCategory;
import store.mealforyou.constant.StockUnit;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 식재료명
    private String origin; // 원산지
    private Integer unitCost; // 단가(quantity가 1일 때 가격)
    private Integer stock; // 재고

    @Enumerated(EnumType.STRING)
    private IngredientCategory category; // 식재료 카테고리

    @Enumerated(EnumType.STRING)
    private StockUnit stockUnit; // 재고 단위
}