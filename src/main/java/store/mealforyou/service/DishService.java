package store.mealforyou.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.mealforyou.dto.DishFormDto;
import store.mealforyou.entity.Dish;
import store.mealforyou.repository.DishRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;

    public List<DishFormDto> getDishes() {
        List<Dish> dishes = dishRepository.findAllWithDishImages();
        List<DishFormDto> dishesDto = new ArrayList<>();
        dishes.forEach(s -> dishesDto.add(DishFormDto.of(s)));

        return dishesDto;
    }
}