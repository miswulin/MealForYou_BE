package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import store.mealforyou.constant.ChangeMode;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_cart_item_ingredient", // 제약조건 이름
                        columnNames = {"cart_item_id", "ingredient_id"}
                )
        }
)
public class CartItemIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟🌟🌟
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItem cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeMode mode = ChangeMode.ABSOLUTE; // 기본값 ABSOLUTE

    @Column(nullable = false)
    private Double quantity;

    // 이 필드는 'ABSOLUTE' 모드일 때 quantity와 같거나, 'RELATIVE' 모드일 때 계산된 최종 수량을 캐싱하는 용도로 사용할 수 있음
    private Double finalQuantity;
}