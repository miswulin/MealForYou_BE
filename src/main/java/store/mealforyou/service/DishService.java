package store.mealforyou.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.mealforyou.constant.ImageType;
import store.mealforyou.constant.ProductCategory;
import store.mealforyou.dto.*;
import store.mealforyou.entity.Dish;
import store.mealforyou.entity.Ingredient;
import store.mealforyou.repository.IngredientRepository;
import store.mealforyou.repository.DishRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;
    private final IngredientRepository ingredientRepository;

    // 3.1.1. 전체메뉴 조회
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

    // 3.1.1. 홈 화면 큐레이션
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

    // 3.1.2. 메뉴 검색
    public List<DishFormDto> searchDishes(String keyword) {
        List<Dish> dishes = dishRepository.findByNameContainingWithDishImages(keyword);

        return dishes.stream()
                .map(DishFormDto::of)
                .collect(Collectors.toList());
    }

    // 4.1.1 제품 소개
    public DishDetailDto getDishDetail(Long dishId) {
        // Repository에서 dishId로 Dish 엔티티 조회
        Dish dish = dishRepository.findByIdWithDetails(dishId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다. id=" + dishId));

        // Dish -> DishDetailDto 변환
        // dishImages 리스트에서 ImageType이 DETAIL_INFO 인 것만 필터링
        List<DishDetailImageDto> imageDtos = dish.getDishImages().stream()
                .filter(image -> image.getImageType() == ImageType.DETAIL_INFO) // DETAIL_INFO 타입 필터링
                .map(image -> new DishDetailImageDto(image.getId(), image.getPath())) // DishImage의 path 사용
                .collect(Collectors.toList());

        // Dish의 기본 구성 재료 DTO로 변환
        Stream<DishIngredientDto> basicIngredientsStream = dish.getDishIngredients().stream()
                .map(dishIngredient -> {
                    Ingredient ingredient = dishIngredient.getIngredient(); // 연결된 Ingredient 엔티티
                    return new DishIngredientDto(
                            ingredient.getId(), // DTO의 ID는 Ingredient의 ID 사용
                            ingredient.getName(),
                            ingredient.getUnitCost(), // Ingredient의 unitCost를 price로 사용
                            dishIngredient.getQuantity(), // DishIngredient의 기본 수량
                            ingredient.getProductCategory()
                    );
                });

        // 이 요리에 적용 가능한 추가 옵션(ADDITIONAL_OPTION) DTO로 변환
        Stream<DishIngredientDto> additionalOptionsStream = ingredientRepository
                .findByProductCategory(ProductCategory.ADDITIONAL_OPTION).stream()
                .map(ingredient -> new DishIngredientDto(
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getUnitCost(),
                        0, // '추가 옵션'은 기본 수량을 0으로 설정
                        ingredient.getProductCategory()
                ));

        // 기본 구성 재료와 추가 옵션 재료를 합친 후, 카테고리별로 그룹핑
        Map<ProductCategory, List<DishIngredientDto>> ingredientsByCategory =
                Stream.concat(basicIngredientsStream, additionalOptionsStream)
                        .collect(Collectors.groupingBy(DishIngredientDto::getCategory));

        // 최종 DTO 반환
        return new DishDetailDto(
                dish.getId(),
                dish.getName(),
                dish.getBasePrice(),
                imageDtos,
                ingredientsByCategory
        );
    }
}