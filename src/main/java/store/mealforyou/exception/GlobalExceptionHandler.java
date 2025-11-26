package store.mealforyou.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

// 전역 예외처리 클래스
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // DTO 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException e) {
        // 첫 번째 fieldError의 defaultMessage 가져오기
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("올바르지 않은 요청입니다.");

        Map<String, Object> body = new HashMap<>();
        body.put("status" , 400);
        body.put("error", "Bad Request");
        body.put("message", message);

        return ResponseEntity.badRequest().body(body);
    }


    // 잘못된 요청 (비밀번호 불일치, 중복 이메일 등)
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

    // JWT 자체가 위조/손상/형식 오류인 경우
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtError(JwtException e) {
        // 내부 메시지는 클라이언트에게 노출 X
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("유효하지 않은 토큰입니다.");
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
    public ResponseEntity<Map<String, Object>> handleException(Exception e, HttpServletRequest request)
    throws Exception {

        String uri = request.getRequestURI();

        if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            throw e;
        }

        log.error("예상치 못한 예외 uri={}, msg={}", uri, e.getMessage(), e);

        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "서버 내부 오류가 발생했습니다.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
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
