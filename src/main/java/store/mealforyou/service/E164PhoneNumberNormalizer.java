package store.mealforyou.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Service;
import store.mealforyou.util.PhoneNumberNormalizer;

@Service
public class E164PhoneNumberNormalizer implements PhoneNumberNormalizer {
    // PhoneNumberUtil은 스레드 세이프하므로 static final로 선언
    private static final PhoneNumberUtil PHONE = PhoneNumberUtil.getInstance(); // 싱글톤 인스턴스를 반환

    @Override // 추상 메서드 toE164()를 오버라이드로 구현
    public String toE164(String raw, String defaultRegion) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("전화번호가 비어 있습니다.");
        }

        // 비숫자 기호는 파서가 처리하지만, 안전하게 선제적으로 strip() 적용
        String candidate = raw.strip();

        try {
            // 국가 번호가 없으면 defaultRegion 기준으로 해석해서 파싱함
            Phonenumber.PhoneNumber proto = PHONE.parse(candidate, defaultRegion);

            if (!PHONE.isValidNumber(proto)) {
                throw new IllegalArgumentException("유효하지 않은 전화번호입니다: " + raw);
            }
            // +8210...형태로 변환
            return PHONE.format(proto, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("전화번호 파싱에 실패했습니다: " + raw, e);
        }
    }
}
