package store.mealforyou.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 4.1.1 제품 소개 - 상세 페이지의 상세정보 이미지
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishDetailImageDto {
    private Long imageId;
    private String imgUrl;
}