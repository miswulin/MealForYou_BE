package store.mealforyou.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store.mealforyou.entity.Dish;

import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    // 전체메뉴 조회
    @Query("SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishImages")
    List<Dish> findAllWithDishImages();

    // 홈 화면 큐레이션
    @Query(value = "SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishImages",
            countQuery = "SELECT COUNT(d) FROM Dish d")
    List<Dish> findAllWithDishImages(Pageable pageable);

    // 3.1.2. 메뉴 검색
    @Query("SELECT DISTINCT d FROM Dish d LEFT JOIN FETCH d.dishImages WHERE d.name LIKE CONCAT('%', :keyword, '%')")
    List<Dish> findByNameContainingWithDishImages(@Param("keyword") String keyword);
}