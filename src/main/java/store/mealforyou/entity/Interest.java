package store.mealforyou.entity;

import store.mealforyou.constant.InterestStatus;

import lombok.*;
import jakarta.persistence.*;

import java.lang.reflect.Member;
import java.time.ZonedDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ZonedDateTime registeredAt; // 관심상품 누른 날짜

    @Enumerated(EnumType.STRING)
    private InterestStatus status; // 상태

//    // 테이블 매핑
//    TODO: private Long memberId; 삭제 및 주석 해제
//    @JoinColumn(name = "member_id")
//    private Member member; // 관심상품 담은 회원
    // 임시 필드: 회원 ID
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    private Dish dish; // 관심상품 한 요리
}