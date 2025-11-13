package store.mealforyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 홈 화면 큐레이션

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MainPageDishesDto {

    // 인기메뉴 5개
    private List<DishFormDto> popularDishes;

    // 최신메뉴 5개
    private List<DishFormDto> newDishes;

    // 전체메뉴 중 추천순 6개
    private List<DishFormDto> recommendedDishes;
}