package store.mealforyou.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import store.mealforyou.entity.Dish;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishImages")
    List<Dish> findAllWithDishImages();
}