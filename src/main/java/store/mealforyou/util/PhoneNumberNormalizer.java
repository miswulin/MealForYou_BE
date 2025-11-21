package store.mealforyou.util;

public interface PhoneNumberNormalizer {
    // 추상 메서드
    String toE164(String raw, String defaultRegion);
}
