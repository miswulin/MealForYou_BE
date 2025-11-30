package store.mealforyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import store.mealforyou.constant.ProductCategory;

import java.util.List;
import java.util.Map;

// 4.1.1 제품 소개 - 메뉴 상세 페이지 응답을 위한 메인 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishDetailDto {
    private Long dishId;
    private String dishName;
    private int basePrice;
    private boolean isInterested;

    // 유저의 건강 태그 이름 리스트
    private List<String> userHealthTags;

    // 사용자 맞춤 추천 옵션 리스트
    private List<DishIngredientDto> recommendedIngredients;

    // 상세 정보 이미지만 담을 리스트
    private List<DishDetailImageDto> dishImages;

    // 카테고리별로 재료/옵션을 담을 맵
    private Map<ProductCategory, List<DishIngredientDto>> ingredientsByCategory;
}