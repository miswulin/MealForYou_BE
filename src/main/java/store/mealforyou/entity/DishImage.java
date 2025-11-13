package store.mealforyou.entity;

import lombok.*;
import jakarta.persistence.*;
import store.mealforyou.constant.ImageType;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DishImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String path; // 이미지 경로
    private Integer sequence; // 순서

    @Enumerated(EnumType.STRING)
    private ImageType imageType; // 이미지 용도 구분: 메인, 상품정보, 캐러셀 이미지

    // 테이블 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish; // 해당 이미지의 요리 Id
}