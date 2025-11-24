package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.entity.CartItem;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByCartId(Long cartId);
}