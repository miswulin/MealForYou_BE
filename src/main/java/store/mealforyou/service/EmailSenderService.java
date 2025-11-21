package store.mealforyou.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from; // 발신자 이메일 주소

    // 인증코드를 이메일로 발송하는 메서드
    public void sendAuthCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setFrom(from);
        message.setSubject("[MealForYou] 이메일 인증 코드 안내");
        message.setText("인증 코드는 다음과 같습니다:\n\n" + code + "\n\n5분 이내로 입력해주세요.");

        // JavaMailSender가 SMTP 서버와 통신하여 실제 이메일을 전송
        mailSender.send(message);
    }
}
