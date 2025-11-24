package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.entity.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // 최신순 정렬하여 조회
    List<Order> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
