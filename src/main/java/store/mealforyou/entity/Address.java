package store.mealforyou.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Address {
    @Column(length = 5, nullable = false)
    private String zipCode; // 우편번호

    @Column(length = 255, nullable = false)
    private String roadAddress; // 도로명 주소

    @Column(length = 255)
    private String detailAddress; // 상세 주소
}
