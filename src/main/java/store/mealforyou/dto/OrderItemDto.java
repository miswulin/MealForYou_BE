package store.mealforyou.dto;

import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private String dishName;
    private String optionDescription;
    private String price;
    private String count;
    private String imageUrl;
}