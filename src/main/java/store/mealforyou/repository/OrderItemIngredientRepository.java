package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.mealforyou.entity.OrderItemIngredient;

import java.util.List;

public interface OrderItemIngredientRepository extends JpaRepository<OrderItemIngredient, Long> {
    // 주문 내역 상세 조회 시 성능을 위한 Fetch Join 사용
    @Query("SELECT oii FROM OrderItemIngredient oii JOIN FETCH oii.ingredient WHERE oii.orderItem.id = :orderItemId")
    List<OrderItemIngredient> findAllByOrderItemId(@Param("orderItemId") Long orderItemId);
}