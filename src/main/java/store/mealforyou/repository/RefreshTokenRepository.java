package store.mealforyou.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final StringRedisTemplate redisTemplate; // Redis 클라이언트

    // properties에서 주입됨
    @Value("${app.jwt.refresh-exp-day}")
    private long refreshExpDay;

    // Refresh Token 저장
    public void save(String email, String refreshToken) {
        String key = key(email);

        redisTemplate.opsForValue().set(
                key, refreshToken,
                Duration.ofDays(refreshExpDay) // TTL 설정 (자동 만료)
        );
    }

    // Refresh Token 조회
    public String find(String email) {
        return redisTemplate.opsForValue().get(key(email));
    }

    // Refresh Token 삭제 (로그아웃/재발급 시 사용)
    public void delete(String email) {
        redisTemplate.delete(key(email));
    }

    // 내부 키 생성 규칙
    private String key(String email) {
        return "refresh: " + email;
    }
}
