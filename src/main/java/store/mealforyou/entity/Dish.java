package store.mealforyou.entity;

import lombok.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 요리명
    private String productInfo; // 상품정보
    private String sort; // 분류 기준
    private Integer basePrice; // 기본구성 가격

    @Lob
    private String recipe; // 레시피

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_dish_image_id")
    private DishImage mainDishImage; // 대표 이미지 1장

    // 테이블 매핑
    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<DishIngredient> dishIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<DishImage> dishImages = new ArrayList<>();

    @OneToMany(mappedBy = "dish", cascade = CascadeType.ALL)
    private List<Interest> interests = new ArrayList<>();
}