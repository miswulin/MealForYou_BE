package store.mealforyou.dto;

import lombok.*;
import store.mealforyou.entity.Dish;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFormDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer basePrice;

    @Builder.Default
    private boolean isInterested = false;

    public static DishFormDto of(Dish dish){
        String repImgUrl = "";
        if (dish.getMainDishImage() != null) {
            repImgUrl = dish.getMainDishImage().getPath();
        }

        return DishFormDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .basePrice(dish.getBasePrice())
                .imageUrl(repImgUrl)
                .isInterested(false)
                .build();
    }
}