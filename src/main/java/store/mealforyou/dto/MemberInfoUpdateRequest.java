package store.mealforyou.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberInfoUpdateRequest(
        @NotBlank
        @Size(min = 2, max = 10)
        String name, // 변경할 이름

        @NotBlank
        String phoneRaw // 사용자가 입력한 전화번호 raw 문자열
) {}
