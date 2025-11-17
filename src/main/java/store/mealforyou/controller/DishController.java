package store.mealforyou.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.DishDetailDto;
import store.mealforyou.dto.DishFormDto;
import store.mealforyou.dto.MainPageDishesDto;
import store.mealforyou.service.DishService;

import java.util.List;

// 전체메뉴 조회

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    // 3.1.1. 전체메뉴 조회
    @GetMapping
    public ResponseEntity<List<DishFormDto>> getDishes(
            @RequestParam(name = "sort", required = false, defaultValue = "default") String sort) {

        return ResponseEntity.ok().body(dishService.getDishes(sort));
    }

    // 3.1.1. 홈 화면 큐레이션
    @GetMapping("/main") // (경로: /api/dishes/main)
    public ResponseEntity<MainPageDishesDto> getMainDishes() {
        return ResponseEntity.ok().body(dishService.getMainPageDishes());
    }

    // 3.1.2. 메뉴 검색
    @GetMapping("/search") // (경로: /api/dishes/search)
    public ResponseEntity<List<DishFormDto>> searchDishes(
            @RequestParam(name = "keyword") String keyword) {
        return ResponseEntity.ok().body(dishService.searchDishes(keyword));
    }

    // 4.1.1 제품 소개
    @GetMapping("/{dishId}") // (경로: /api/dishes/{dishId})
    public ResponseEntity<DishDetailDto> getDishDetail(
            @PathVariable("dishId") Long dishId) {
        return ResponseEntity.ok().body(dishService.getDishDetail(dishId));
    }
}