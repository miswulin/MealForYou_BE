package store.mealforyou.entity;

import store.mealforyou.constant.InterestStatus;

import lombok.*;
import jakarta.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ZonedDateTime registeredAt; // 관심상품 누른 날짜

    @Enumerated(EnumType.STRING)
    private InterestStatus status; // 상태


    // 테이블 매핑
    @JoinColumn(name = "member_id")
    private Long memberId; // 관심상품 담은 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish; // 관심상품 한 요리
}