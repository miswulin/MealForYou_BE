package store.mealforyou.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "members")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id; // 기본키

    @Column(nullable = false, length = 100)
    private String email; // 이메일 (로그인 id)

    @Column(nullable = false, length = 10)
    private String name; // 이름

    @Column(nullable = false, length = 50)
    private String password; // 비밀번호

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneE16; // 전화번호 (e.164 정규화)

    @Embedded
    private Address address;

    // 마이페이지에서 수정하기 위한 메서드
    public void updateName(String name) { this.name = name; }
    public void updatePassword(String password) { this.password = password; }
    public void updatePhone(String phone) { this.phoneE16 = phone; }
    public void updateAddress(Address address) { this.address = address; }
}
