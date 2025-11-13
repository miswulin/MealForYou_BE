package store.mealforyou.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.mealforyou.dto.DishFormDto;
import store.mealforyou.entity.Dish;
import store.mealforyou.repository.DishRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import store.mealforyou.dto.MainPageDishesDto;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;

    // 전체메뉴 조회
    public List<DishFormDto> getDishes(String sort) {
        List<Dish> dishes = dishRepository.findAllWithDishImages();

        // 'sort' 값에 따라 정렬
        switch (sort) {
            case "popular":
                dishes.sort(Comparator.comparing(Dish::getPopularityRank,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;

            case "new":
                dishes.sort(Comparator.comparing(Dish::getId).reversed());
                break;

            case "recommend":
                dishes.sort(Comparator.comparing(Dish::getRecommendRank,
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;

            default:
                dishes.sort(Comparator.comparing(Dish::getId));
                break;
        }

        return dishes.stream()
                .map(DishFormDto::of)
                .collect(Collectors.toList());
    }

    // 홈 화면 큐레이션
    public MainPageDishesDto getMainPageDishes() {

        // 인기 상품 5개 조회
        Pageable popularPage = PageRequest.of(0, 5, Sort.by("popularityRank").ascending());
        List<Dish> popularDishesEntity = dishRepository.findAllWithDishImages(popularPage);
        List<DishFormDto> popularDishesDto = popularDishesEntity.stream()
                .map(DishFormDto::of)
                .collect(Collectors.toList());

        // 최신 상품 5개 조회
        Pageable newPage = PageRequest.of(0, 5, Sort.by("id").descending());
        List<Dish> newDishesEntity = dishRepository.findAllWithDishImages(newPage);
        List<DishFormDto> newDishesDto = newDishesEntity.stream()
                .map(DishFormDto::of)
                .collect(Collectors.toList());

        // 추천 상품 6개 조회
        Pageable recommendPage = PageRequest.of(0, 6, Sort.by("recommendRank").ascending());
        List<Dish> recommendedDishesEntity = dishRepository.findAllWithDishImages(recommendPage);
        List<DishFormDto> recommendedDishesDto = recommendedDishesEntity.stream()
                .map(DishFormDto::of)
                .collect(Collectors.toList());

        return new MainPageDishesDto(popularDishesDto, newDishesDto, recommendedDishesDto);
    }
}