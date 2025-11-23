package store.mealforyou.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 요청 (입력값 오류, 인증코드 불일치 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    // 인증 필요 또는 정책 위반 (이메일 미인증, refresh 실패 등)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleUnathorized(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    // 로그인 실패 (아이디 없거나 비밀번호 틀림)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<?> handleLoginFailure(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    // 기타 예상하지 못한 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception e) {
        e.printStackTrace(); // 콘솔에 실제 오류 출력
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 내부 오류가 발생했습니다: " + e.getMessage());
    }

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
