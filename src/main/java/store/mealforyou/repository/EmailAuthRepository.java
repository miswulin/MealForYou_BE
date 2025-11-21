package store.mealforyou.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class EmailAuthRepository {

    private final StringRedisTemplate redisTemplate;

    // 인증코드를 저장할 때 사용할 Redis key prefix
    private static final String CODE_KEY_PREFIX = "email:code:";

    // 이메일 인증이 정상적으로 완료되었음을 저장하는 key prefix
    private static final String VERIFIED_KEY_PREFIX = "email:verified";

    // 인증코드 TTL: 5분
    private static final Duration CODE_TTL = Duration.ofMinutes(5);

    // 인증 완료 플래그 TTL: 24시간
    private static final Duration VERIFIED_TTL = Duration.ofHours(24);

    // 4자리의 인증 코드를 저장
    public void savedCode(String email, String code) {
        String key = CODE_KEY_PREFIX + email;
        // Redis에 코드 저장 + TTL 설정
        redisTemplate.opsForValue().set(key, code, CODE_TTL);
    }

    // 저장된 인증코드를 조회
    public String getCode(String email) {
        String key = CODE_KEY_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    // 인증코드를 Redis에서 제거
    public void deleteCode(String email) {
        redisTemplate.delete(CODE_KEY_PREFIX + email);
    }

    // 인증 완료 여부를 true로 설정
    public void markVerified(String email) {
        // verified 값은 "true"로 저장하지만, 실제로 이 값은 내부적으로는 크게 중요 X
        // 핵심은 "이 이메일은 인증이 끝났다는 사실"과 "24시간동안만 유지된다"는 것
        redisTemplate.opsForValue().set(
                VERIFIED_KEY_PREFIX + email,
                "true",
                VERIFIED_TTL
        );
    }

    // 인증이 완료된 이메일인지 확인
    public boolean isVerified(String email) {
        String key = VERIFIED_KEY_PREFIX + email;
        String value = redisTemplate.opsForValue().get(key);
        return value != null; // 존재하면 인증된 것으로 간주
    }
}
