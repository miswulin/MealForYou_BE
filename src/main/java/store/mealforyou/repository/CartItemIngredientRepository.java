package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.mealforyou.entity.CartItemIngredient;

import java.util.List;

public interface CartItemIngredientRepository extends JpaRepository<CartItemIngredient, Long> {
    // 옵션 상세 정보 조회 시 Ingredient 테이블까지 동시 조회
    @Query("SELECT cii FROM CartItemIngredient cii JOIN FETCH cii.ingredient WHERE cii.cartItem.id = :cartItemId")
    List<CartItemIngredient> findAllByCartItemId(@Param("cartItemId") Long cartItemId);
}
