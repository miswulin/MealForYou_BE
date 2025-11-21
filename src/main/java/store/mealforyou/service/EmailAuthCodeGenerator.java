package store.mealforyou.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class EmailAuthCodeGenerator {

    // SecureRandom은 Math.random()보다 훨씬 예측하기 어려운 난수 생성기이므로 인증번호와 같은 난수값 생성에 적합하다.
    private final SecureRandom random = new SecureRandom();

    // 4자리 숫자 인증코드를 생성
    public String generateCode() {
        // 1000~9999 사이의 값 생성 (항상 4자리 보장)
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }
}
