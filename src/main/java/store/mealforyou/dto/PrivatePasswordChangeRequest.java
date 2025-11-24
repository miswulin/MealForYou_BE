package store.mealforyou.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PrivatePasswordChangeRequest(
        @NotBlank
        String currentPassword, // 현재 사용 중인 비밀번호

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$*])[A-Za-z\\d!@#$*]{8,16}$",
                message = "비밀번호는 8~16자, 소문자·숫자·특수문자(!@#$*) 각 1개 이상 포함해야 합니다."
        )
        String newPassword, // 새 비밀번호

        @NotBlank
        String newPasswordConfirm // 새 비밀번호 재확인
) {}
