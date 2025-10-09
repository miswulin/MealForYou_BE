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

    private static ModelMapper modelMapper = new ModelMapper();

    public static DishFormDto of(Dish dish){
        DishFormDto dto = modelMapper.map(dish, DishFormDto.class);

        if (dish.getMainDishImage() != null) {
            dto.imageUrl = dish.getMainDishImage().getPath();
        }
        return dto;
    }
}