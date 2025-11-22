package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}