package store.mealforyou.entity;

import store.mealforyou.constant.Unit;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DishIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double quantity; // 해당 식재료의 수량

    @Enumerated(EnumType.STRING)
    private Unit unit; // 해당 식재료의 단위


    // 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish; // 해당 식재료가 들어가는 요리 Id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient; // 해당 요리에 들어가는 식재료 Id
}