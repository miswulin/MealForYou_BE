package store.mealforyou.dto;

import lombok.Getter;
import org.modelmapper.ModelMapper;
import store.mealforyou.entity.Dish;

@Getter
public class DishFormDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer basePrice;

    public static DishFormDto of(Dish dish){
        DishFormDto dto = new DishFormDto(); // 새 DTO 객체 생성

        dto.id = dish.getId(); // 수동 매핑
        dto.name = dish.getName();
        dto.basePrice = dish.getBasePrice();

        if (dish.getMainDishImage() != null) {
            dto.imageUrl = dish.getMainDishImage().getPath();
        }
        return dto;
    }
}