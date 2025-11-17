package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import store.mealforyou.constant.ProductCategory;
import store.mealforyou.entity.Ingredient;

import java.util.List;

// 4.1.1 제품 소개

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    // 특정 카테고리에 해당하는 모든 옵션 조회
    List<Ingredient> findByProductCategory(ProductCategory productCategory);
}