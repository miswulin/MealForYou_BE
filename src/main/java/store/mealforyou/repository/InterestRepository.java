package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.mealforyou.constant.InterestStatus;
import store.mealforyou.entity.Interest;

import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    // 목록 조회: 해당 유저가 찜한 모든 요리의 ID를 조회
    @Query("SELECT i.dish.id FROM Interest i WHERE i.memberId = :memberId AND i.status = :status")
    // TODO: 현재 로그인 회원 ID 가져오는 로직 구현
    // @Query("SELECT i.dish.id FROM Interest i WHERE i.member.id = :memberId AND i.status = :status")
    List<Long> findDishIdsByMemberIdAndStatus(@Param("memberId") Long memberId, @Param("status") InterestStatus status);

    // 상세 조회: 해당 유저가 해당 요리를 찜했는지 확인
    boolean existsByDishIdAndMemberIdAndStatus(Long dishId, Long memberId, InterestStatus status);

    // 관심상품 토글 기능을 위해 엔티티 조회
    Optional<Interest> findByDishIdAndMemberId(Long dishId, Long memberId);

    // 관심상품 목록 표시: 최근 추가순 정렬
    @Query("SELECT i FROM Interest i " +
            "JOIN FETCH i.dish d " +
            "LEFT JOIN FETCH d.dishImages " +
            "WHERE i.memberId = :memberId AND i.status = 'ACTIVE' " +
            "ORDER BY i.registeredAt DESC")
    List<Interest> findMyActiveInterests(@Param("memberId") Long memberId);

    // 관심 상품 일괄 해제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Interest i SET i.status = 'DELETED' " +
            "WHERE i.memberId = :memberId AND i.dish.id IN :dishIds")
    void bulkDeleteInterests(@Param("memberId") Long memberId, @Param("dishIds") List<Long> dishIds);
}