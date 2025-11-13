package store.mealforyou.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.mealforyou.dto.DishFormDto;
import store.mealforyou.service.DishService;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService dishService;

    @GetMapping
    public ResponseEntity<List<DishFormDto>> getDishes() {
        return ResponseEntity.ok().body(dishService.getDishes());
    }
}