package store.mealforyou.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.HashMap;

// 프로젝트 전역의 예외를 처리하는 핸들러
@RestControllerAdvice
public class GlobalExceptionHandler {

    // EntityNotFoundException 발생 시 404 Not Found 응답을 반환
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {

        // API 명세서에 맞춘 JSON 응답 형태
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "404");
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage()); // DishService의 "해당 메뉴를 찾을 수 없습니다..." 메시지가 여기에 담김

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}