package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.DishDetailDto;
import store.mealforyou.dto.DishFormDto;
import store.mealforyou.dto.MainPageDishesDto;
import store.mealforyou.service.DishService;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@Tag(name = "메뉴 API", description = "홈 화면 메뉴 조회, 전체메뉴 조회, 메뉴 검색, 메뉴 상세 조회, 관심 상품 등록/조회/해제 기능을 제공합니다.")
public class DishController {
    private final DishService dishService;

    // 3.1.1. 전체메뉴 조회
    @GetMapping
    @Operation(
            summary = "전체메뉴 조회",
            description = "DB에 존재하는 모든 메뉴를 조회합니다."
    )
    public ResponseEntity<List<DishFormDto>> getDishes(
            @RequestParam(name = "sort", required = false, defaultValue = "default") String sort) {
        return ResponseEntity.ok().body(dishService.getDishes(sort));
    }

    // 3.1.1. 홈 화면 큐레이션
    @GetMapping("/main")
    @Operation(
            summary = "홈 화면 큐레이션",
            description = "인기 상품 5개, 최신 상품 5개, 추천 상품 6개를 조회합니다."
    )
    public ResponseEntity<MainPageDishesDto> getMainDishes() {
        return ResponseEntity.ok().body(dishService.getMainPageDishes());
    }

    // 3.1.2. 메뉴 검색
    @GetMapping("/search")
    @Operation(
            summary = "메뉴 검색",
            description = "사용자가 검색한 키워드가 메뉴 이름에 포함된 메뉴를 조회합니다."
    )
    public ResponseEntity<List<DishFormDto>> searchDishes(
            @RequestParam(name = "keyword") String keyword) {
        return ResponseEntity.ok().body(dishService.searchDishes(keyword));
    }

    // 4.1.1 제품 소개
    @GetMapping("/{dishId}")
    @Operation(
            summary = "제품 소개",
            description = "해당 메뉴의 상세정보를 조회합니다."
    )
    public ResponseEntity<DishDetailDto> getDishDetail(
            @PathVariable("dishId") Long dishId) {
        return ResponseEntity.ok().body(dishService.getDishDetail(dishId));
    }

    // 8.1 관심 상품 등록/해제
    @PostMapping("/{dishId}/interest")
    @Operation(
            summary = "관심 상품 등록/해제",
            description = "관심 상품을 등록/해제합니다."
    )
    public ResponseEntity<Boolean> toggleInterest(@PathVariable("dishId") Long dishId) {
        boolean isInterested = dishService.toggleInterest(dishId);
        return ResponseEntity.ok().body(isInterested);
    }

    // 8.1.1 관심상품 목록 표시: 최근 추가순 정렬
    @GetMapping("/interest")
    @Operation(
            summary = "관심상품 목록 표시: 최근 추가순 정렬",
            description = "관심상품 목록을 최신순으로 조회합니다."
    )
    public ResponseEntity<List<DishFormDto>> getMyInterests() {
        return ResponseEntity.ok().body(dishService.getMyInterests());
    }

    // 8.1.2 관심상품 해제
    @DeleteMapping("/interest")
    @Operation(
            summary = "관심상품 해제",
            description = "관심상품 목록에서 관심상품 상태를 일괄 해제합니다."
    )
    public ResponseEntity<Void> deleteInterests(@RequestBody List<Long> dishIds) {
        dishService.deleteInterests(dishIds);
        return ResponseEntity.ok().build();
    }
}