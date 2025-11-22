package store.mealforyou.entity;

import lombok.*;
import jakarta.persistence.*;
import store.mealforyou.constant.ProductCategory;
import store.mealforyou.constant.ProductTag;

@Entity
@Getter @Setter
@NoArgsConstructor(/* access = AccessLevel.PROTECTED */)
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 식재료명

    private Integer unitCost; // 가격

    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory; // 소스/맵기, 기본옵션, 추가옵션

    @Enumerated(EnumType.STRING)
    private ProductTag productTag; // 태그
}