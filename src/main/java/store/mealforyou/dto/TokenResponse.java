package store.mealforyou.dto;

// 액세스/리프레시 토큰 페이로드를 일관된 포맷으로 응답
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType, // Bearer 타입 토큰
        long expiresIn // 액세스 만료시간 (초)
) {
}
