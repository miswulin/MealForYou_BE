package store.mealforyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import store.mealforyou.constant.ProductCategory;
import store.mealforyou.constant.ProductTag;

// 4.1.1 제품 소개 - 상세 페이지의 재료/옵션 정보
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishIngredientDto {
    private Long dishIngredientId;
    private String name;
    private int price;
    private int quantity; // 기본 선택 수량: 기본 구성이면 1 이상, 변경 옵션이면 0
    private ProductCategory category;
    private ProductTag productTag;
}