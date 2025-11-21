package store.mealforyou.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^\\d{4}$", message = "인증 코드는 4자리 숫자입니다.")
        String code
) {}
