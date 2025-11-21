package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item_ingredient",
        uniqueConstraints = @UniqueConstraint(columnNames = {"order_item_id", "ingredient_id"}))
@Getter @Setter
@NoArgsConstructor
public class OrderItemIngredient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(nullable = false) // Default 'ABSOLUTE'
    private String mode = "ABSOLUTE";

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "final_quantity")
    private Double finalQuantity;
}