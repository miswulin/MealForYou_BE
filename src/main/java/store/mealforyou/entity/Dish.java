package store.mealforyou.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import store.mealforyou.constant.ImageType;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 요리명
    private Integer basePrice; // 기본구성 가격

    private Integer popularityRank; // 인기순
    private Integer recommendRank;  // 추천순
    // 최신순: id 사용

    // 테이블 매핑
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishIngredient> dishIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishImage> dishImages = new ArrayList<>();

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interest> interests = new ArrayList<>();

    public DishImage getMainDishImage() {
        if (this.dishImages == null || this.dishImages.isEmpty()) {
            return null;
        }

        // 전체 이미지 리스트에서 'REPRESENTATIVE' 타입의 이미지를 필터링하여 첫 번째 것을 반환
        return this.dishImages.stream()
                .filter(image -> image.getImageType() == ImageType.REPRESENTATIVE)
                .findFirst()
                .orElse(null); // 대표 이미지가 없으면 null 반환
    }
}