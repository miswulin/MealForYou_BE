package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import store.mealforyou.constant.OrderStatus;
import store.mealforyou.constant.PaymentType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // 기존: nullable = false -> 회원이 탈퇴되면 null로 만ㄷ르 수 있도록 true로 변경함
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "zipCode", column = @Column(name = "delivery_zip_code")),
            @AttributeOverride(name = "roadAddress", column = @Column(name = "delivery_road_address")),
            @AttributeOverride(name = "detailAddress", column = @Column(name = "delivery_detail_address"))
    })
    private Address deliveryAddress;

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

    // 양방향 매핑
    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();
}