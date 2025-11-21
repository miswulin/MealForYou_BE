package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.mealforyou.repository.EmailAuthRepository;

@Service
@RequiredArgsConstructor
public class EmailAuthService {
    private final EmailAuthRepository emailAuthRepository;
    private final EmailAuthCodeGenerator codeGenerator;
    private final EmailSenderService emailSenderService;

    // 인증 코드를 생성하고 이메일로 발송하며, Redis에 저장하는 메서드
    public void sendCode(String email) {
        // 4자리 인증코드 생성
        String code = codeGenerator.generateCode();

        // Redis에 인증코드 저장
        emailAuthRepository.savedCode(email, code);

        // 사용자에게 인증코드 이메일 발송
        emailSenderService.sendAuthCode(email, code);
    }

    // 사용자가 입력한 인증코드를 검증하는 메서드
    public void verifyCode(String email, String inputCode) {
        // Redis에서 실제 저장된 인증코드 조회
        String savedCode = emailAuthRepository.getCode(email);

        if (savedCode == null) {
            throw new IllegalArgumentException("인증코드가 존재하지 않거나 시간이 만료되었습니다.");
        }

        if (!savedCode.equals(inputCode)) {
            throw new IllegalArgumentException("인증코드가 올바르지 않습니다.");
        }

        // 인증 성공: 인증완료 플래그 24시간 저장
        emailAuthRepository.markVerified(email);

        // 인증코드 삭제
        emailAuthRepository.deleteCode(email);
    }
}