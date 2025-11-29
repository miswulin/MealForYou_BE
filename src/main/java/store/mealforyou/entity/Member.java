package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import store.mealforyou.constant.ProductTag;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // 기본키

    @Column(nullable = false, length = 100)
    private String email; // 이메일 (로그인 id)

    @Column(nullable = false, length = 10)
    private String name; // 이름

    @Column(nullable = false, length = 60) // BCrypt 해시는 보통 60자
    private String password; // 비밀번호

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneE164; // 전화번호 (e.164 정규화)

    @Embedded
    private Address address;

    // 회원이 선호하는 식단 태그 목록 (최대 3개 선택)
    // 별도의 조인 테이블 member_health_tags에 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_health_tags",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "health_tag", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Set<ProductTag> healthTags = new HashSet<>();

    // 마이페이지에서 수정하기 위한 메서드
    public void updateName(String name) { this.name = name; }
    public void updatePassword(String password) { this.password = password; }
    public void updatePhone(String phone) { this.phoneE164 = phone; }
    public void updateAddress(Address address) { this.address = address; }

    // 선호 식단 교체 메서드
    public void updateHealthTags(Set<ProductTag> tags) {
        this.healthTags.clear();
        if (tags != null) {
            this.healthTags.addAll(tags);
        }
    }
}
