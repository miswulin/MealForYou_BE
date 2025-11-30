package store.mealforyou.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import store.mealforyou.constant.ImageType;
import store.mealforyou.constant.ProductCategory;
import store.mealforyou.constant.ProductTag;
import store.mealforyou.dto.*;
import store.mealforyou.entity.Dish;
import store.mealforyou.entity.Ingredient;
import store.mealforyou.entity.Interest;
import store.mealforyou.entity.Member;
import store.mealforyou.repository.IngredientRepository;
import store.mealforyou.repository.DishRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import store.mealforyou.constant.InterestStatus;
import store.mealforyou.repository.InterestRepository;
import store.mealforyou.repository.MemberRepository;

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
    private final MemberRepository memberRepository;

    // 현재 로그인한 회원의 ID를 반환하는 메서드
    // return값: 로그인된 회원 ID, 비로그인시 null
    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 비로그인인 경우 null
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        // 인증된 사용자의 이메일(username) 가져오기
        String email = authentication.getName();

        // 이메일로 회원 조회
        return memberRepository.findByEmail(email).orElse(null);
    }

    // 유저의 건강 태그 정보 가져오기
    private List<ProductTag> getUserHealthTags(Member member) {
        if (member == null) {
            return List.of(); // 비로그인 시 빈 리스트 반환
        }
        return new ArrayList<>(member.getHealthTags());
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
        Member currentMember = getCurrentMember();
        List<Long> likedDishIds;
        if (currentMember != null) {
            likedDishIds = interestRepository.findDishIdsByMemberAndStatus(currentMember, InterestStatus.ACTIVE);
        } else {
            likedDishIds = List.of();
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
        Member currentMember = getCurrentMember();
        List<Long> likedDishIds = (currentMember != null)
                ? interestRepository.findDishIdsByMemberAndStatus(currentMember, InterestStatus.ACTIVE)
                : List.of();

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
        Member currentMember = getCurrentMember();
        List<Long> likedDishIds = (currentMember != null)
                ? interestRepository.findDishIdsByMemberAndStatus(currentMember, InterestStatus.ACTIVE)
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
        // Dish 엔티티 조회
        Dish dish = dishRepository.findByIdWithDetails(dishId)
                .orElseThrow(() -> new EntityNotFoundException("해당 메뉴를 찾을 수 없습니다. id=" + dishId));

        // 이미지 변환
        List<DishDetailImageDto> imageDtos = dish.getDishImages().stream()
                .filter(image -> image.getImageType() == ImageType.DETAIL_INFO)
                .map(image -> new DishDetailImageDto(image.getId(), image.getPath()))
                .collect(Collectors.toList());

        // 재료 리스트 생성
        List<DishIngredientDto> allIngredients = dish.getDishIngredients().stream()
                .map(dishIngredient -> {
                    Ingredient ingredient = dishIngredient.getIngredient();
                    return new DishIngredientDto(
                            ingredient.getId(),
                            ingredient.getName(),
                            ingredient.getUnitCost(),
                            dishIngredient.getQuantity(),
                            ingredient.getProductCategory(),
                            ingredient.getProductTag()
                    );
                })
                .collect(Collectors.toList());

        // 카테고리별 그룹핑
        Map<ProductCategory, List<DishIngredientDto>> ingredientsByCategory = allIngredients.stream()
                .collect(Collectors.groupingBy(DishIngredientDto::getCategory));

        // 추천 옵션 필터링
        Member currentMember = getCurrentMember();
        List<ProductTag> userTags = getUserHealthTags(currentMember);

        List<String> userHealthTags = userTags.stream()
                .map(ProductTag::getDescription)
                .collect(Collectors.toList());

        List<DishIngredientDto> recommendedIngredients = allIngredients.stream()
                .filter(dto -> dto.getProductTag() != null)
                .filter(dto -> userTags.contains(dto.getProductTag()))
                .collect(Collectors.toList());

        // 관심상품 여부 확인
        boolean isInterested = false;
        if (currentMember != null) {
            isInterested = interestRepository.existsByDishIdAndMemberAndStatus(dishId, currentMember, InterestStatus.ACTIVE);
        }

        // 최종 반환
        return new DishDetailDto(
                dish.getId(),
                dish.getName(),
                dish.getBasePrice(),
                isInterested,
                userHealthTags,
                recommendedIngredients,
                imageDtos,
                ingredientsByCategory
        );
    }

    // 8.1 관심 상품 등록/해제
    public boolean toggleInterest(Long dishId) {
        Member currentMember = getCurrentMember();

        // 로그인 여부 확인
        if (currentMember == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        // 기존 관심 내역 조회
        Interest interest = interestRepository.findByDishIdAndMember(dishId, currentMember).orElse(null);

        if (interest == null) {
            // 관심 설정 내역이 없으면 새로 생성 (ACTIVE)
            Dish dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 요리입니다."));

            Interest newInterest = new Interest();
            newInterest.setDish(dish);
            newInterest.setMember(currentMember);
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
        Member currentMember = getCurrentMember();
        if (currentMember == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        // 관심상품 최신순 조회
        List<Interest> interests = interestRepository.findMyActiveInterests(currentMember);

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
        Member currentMember = getCurrentMember();
        if (currentMember == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        if (dishIds == null || dishIds.isEmpty()) {
            return; // 삭제할 게 없으면 종료
        }

        // Bulk Update 쿼리 실행
        interestRepository.bulkDeleteInterests(currentMember, dishIds);
    }
}