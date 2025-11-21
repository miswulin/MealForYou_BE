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
import store.mealforyou.entity.Interest;
import store.mealforyou.repository.IngredientRepository;
import store.mealforyou.repository.DishRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import store.mealforyou.constant.InterestStatus;
import store.mealforyou.repository.InterestRepository;

import java.time.ZonedDateTime;
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
    private final InterestRepository interestRepository;


    // 현재 로그인한 회원의 ID를 반환하는 메서드
    // 추후 SecurityContextHolder나 JWT 사용해서 실제 ID 반환하도록 수정 필요
    // return값: 로그인된 회원 ID, 비로그인시 null
    private Long getCurrentMemberId() {

        // TODO: 현재 로그인 회원 ID 가져오는 로직 구현

//        return null; // 가정: 비로그인
         return 1L; // 테스트: ID 1 회원으로 로그인된 상태
    }

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

            case "low_price":
                dishes.sort(Comparator.comparing(Dish::getBasePrice));
                break;

            default:
                dishes.sort(Comparator.comparing(Dish::getId));
                break;
        }

        // 현재 로그인 유저의 관심상품 목록 조회
        Long currentMemberId = getCurrentMemberId();
        List<Long> likedDishIds;
        if (currentMemberId != null) {
            likedDishIds = interestRepository.findDishIdsByMemberIdAndStatus(currentMemberId, InterestStatus.ACTIVE);
        } else {
            likedDishIds = List.of(); // 빈 리스트
        }

        return dishes.stream()
                .map(dish -> {
                    DishFormDto dto = DishFormDto.of(dish);
                    if (likedDishIds.contains(dish.getId())) {
                        dto.setInterested(true);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 3.1.1. 홈 화면 큐레이션
    public MainPageDishesDto getMainPageDishes() {

        // 로그인 유저 ID 및 관심상품 목록 조회
        Long currentMemberId = getCurrentMemberId();
        List<Long> likedDishIds = (currentMemberId != null)
                ? interestRepository.findDishIdsByMemberIdAndStatus(currentMemberId, InterestStatus.ACTIVE)
                : List.of();

        // 공통 변환 로직 (Dish -> DishFormDto + isInterested)
        java.util.function.Function<Dish, DishFormDto> convertToDto = dish -> {
            DishFormDto dto = DishFormDto.of(dish);
            if (likedDishIds.contains(dish.getId())) {
                dto.setInterested(true);
            }
            return dto;
        };

        // 인기 상품 5개 조회
        Pageable popularPage = PageRequest.of(0, 5, Sort.by("popularityRank").ascending());
        List<Dish> popularDishesEntity = dishRepository.findAllWithDishImages(popularPage);
        List<DishFormDto> popularDishesDto = popularDishesEntity.stream()
                .map(convertToDto)
                .collect(Collectors.toList());

        // 최신 상품 5개 조회
        Pageable newPage = PageRequest.of(0, 5, Sort.by("id").descending());
        List<Dish> newDishesEntity = dishRepository.findAllWithDishImages(newPage);
        List<DishFormDto> newDishesDto = newDishesEntity.stream()
                .map(convertToDto)
                .collect(Collectors.toList());

        // 추천 상품 6개 조회
        Pageable recommendPage = PageRequest.of(0, 6, Sort.by("recommendRank").ascending());
        List<Dish> recommendedDishesEntity = dishRepository.findAllWithDishImages(recommendPage);
        List<DishFormDto> recommendedDishesDto = recommendedDishesEntity.stream()
                .map(convertToDto)
                .collect(Collectors.toList());

        return new MainPageDishesDto(popularDishesDto, newDishesDto, recommendedDishesDto);
    }

    // 3.1.2. 메뉴 검색
    public List<DishFormDto> searchDishes(String keyword) {
        List<Dish> dishes = dishRepository.findByNameContainingWithDishImages(keyword);


        // 관심 여부 처리
        Long currentMemberId = getCurrentMemberId();
        List<Long> likedDishIds = (currentMemberId != null)
                ? interestRepository.findDishIdsByMemberIdAndStatus(currentMemberId, InterestStatus.ACTIVE)
                : List.of();

        return dishes.stream()
                .map(dish -> {
                    DishFormDto dto = DishFormDto.of(dish);
                    if (likedDishIds.contains(dish.getId())) {
                        dto.setInterested(true);
                    }
                    return dto;
                })
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

        // 관심상품 여부 확인
        boolean isInterested = false;
        Long currentMemberId = getCurrentMemberId();
        if (currentMemberId != null) {
            isInterested = interestRepository.existsByDishIdAndMemberIdAndStatus(dishId, currentMemberId, InterestStatus.ACTIVE);
        }

        // 최종 DTO 반환
        return new DishDetailDto(
                dish.getId(),
                dish.getName(),
                dish.getBasePrice(),
                isInterested,
                imageDtos,
                ingredientsByCategory
        );
    }

    // 8.1 관심 상품 등록/해제
    public boolean toggleInterest(Long dishId) {
        Long currentMemberId = getCurrentMemberId();

        // 로그인 여부 확인
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다."); // 비로그인 오류 발생
        }

        // 기존 관심 내역 조회
        Interest interest = interestRepository.findByDishIdAndMemberId(dishId, currentMemberId)
                .orElse(null);

        if (interest == null) {
            // 관심 설정 내역이 없으면 새로 생성 (ACTIVE)
            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 요리입니다."));

            Interest newInterest = new Interest();
            newInterest.setDish(dish);
            newInterest.setMemberId(currentMemberId);
            newInterest.setStatus(InterestStatus.ACTIVE);
            newInterest.setRegisteredAt(ZonedDateTime.now());

            interestRepository.save(newInterest);
            return true; // 관심상품 등록
        } else {
            // 관심 설정 내역이 있으면 토글 상태 변경
            if (interest.getStatus() == InterestStatus.ACTIVE) {
                interest.setStatus(InterestStatus.DELETED);
                return false; // 관심상품 해제
            } else {
                interest.setStatus(InterestStatus.ACTIVE);
                interest.setRegisteredAt(ZonedDateTime.now()); // 재등록 시간 갱신
                return true; // 관심상품 등록
            }
        }
    }

    // 8.1.1 관심상품 목록 표시: 최근 추가순 정렬
    public List<DishFormDto> getMyInterests() {
        Long currentMemberId = getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        // 관심상품 최신순 조회
        List<Interest> interests = interestRepository.findMyActiveInterests(currentMemberId);

        // Interest -> DishFormDto 변환
        return interests.stream()
                .map(interest -> {
                    Dish dish = interest.getDish();
                    DishFormDto dto = DishFormDto.of(dish);
                    dto.setInterested(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 8.1.2 관심상품 해제
    public void deleteInterests(List<Long> dishIds) {
        Long currentMemberId = getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        if (dishIds == null || dishIds.isEmpty()) {
            return; // 삭제할 게 없으면 종료
        }

        // Bulk Update 쿼리 실행
        interestRepository.bulkDeleteInterests(currentMemberId, dishIds);
    }
}