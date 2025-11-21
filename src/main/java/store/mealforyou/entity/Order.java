package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import store.mealforyou.constant.OrderStatus;
import store.mealforyou.constant.PaymentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // SQL 테이블명 'orders' 명시 (SQL 예약어 충돌 방지)
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "shipping_fee")
    private Integer shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 양방향 매핑 (선택사항이지만 조회 편의를 위해 추천)
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();
}