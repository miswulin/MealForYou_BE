package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter @Setter
@NoArgsConstructor
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price; // 주문 당시 가격

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}