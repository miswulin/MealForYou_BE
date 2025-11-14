package store.mealforyou.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 이메일 인증 코드 발급 요청 DTO
public record SendCodeRequest(@Email @NotBlank String email) {}
