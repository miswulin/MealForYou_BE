package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMemberId(Long memberId);
}
