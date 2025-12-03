package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.mealforyou.constant.InterestStatus;
import store.mealforyou.entity.Interest;
import store.mealforyou.entity.Member;

import java.util.List;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    // 회원 탈퇴 시, 해당 회원의 관심상품 전체 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Interest i WHERE i.member = :member")
    void deleteByMember(@Param("member") Member member);

    // 목록 조회: 해당 유저가 찜한 모든 요리의 ID를 조회
    @Query("SELECT i.dish.id FROM Interest i WHERE i.member = :member AND i.status = :status")
    List<Long> findDishIdsByMemberAndStatus(@Param("member") Member member, @Param("status") InterestStatus status);

    // 상세 조회: 해당 유저가 해당 요리를 찜했는지 확인
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Interest i WHERE i.dish.id = :dishId AND i.member = :member AND i.status = :status")
    boolean existsByDishIdAndMemberAndStatus(@Param("dishId") Long dishId, @Param("member") Member member, @Param("status") InterestStatus status);

    // 관심상품 토글 기능을 위해 엔티티 조회
    @Query("SELECT i FROM Interest i WHERE i.dish.id = :dishId AND i.member = :member")
    Optional<Interest> findByDishIdAndMember(@Param("dishId") Long dishId, @Param("member") Member member);

    // 관심상품 목록 표시: 최근 추가순 정렬
    @Query("SELECT i FROM Interest i " +
            "JOIN FETCH i.dish d " +
            "LEFT JOIN FETCH d.dishImages " +
            "WHERE i.member = :member AND i.status = 'ACTIVE' " +
            "ORDER BY i.registeredAt DESC")
    List<Interest> findMyActiveInterests(@Param("member") Member member);

    // 관심 상품 일괄 해제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Interest i SET i.status = 'DELETED' " +
            "WHERE i.member = :member AND i.dish.id IN :dishIds")
    void bulkDeleteInterests(@Param("member") Member member, @Param("dishIds") List<Long> dishIds);
}