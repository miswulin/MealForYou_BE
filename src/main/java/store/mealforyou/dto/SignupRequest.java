package store.mealforyou.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Email @NotBlank
        String email,
        @NotBlank @Size(min = 2, max = 10)
        String name,

        // 비밀번호 정책: 8-16자이며 영어 소문자, 숫자, 특수문자 !@#$*를 최소 하나 포함
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$*])[A-Za-z\\d!@#$*]{8,16}$",
                message = "비밀번호는 8~16자, 소문자·숫자·특수문자(!@#$*) 각 1개 이상 포함해야 합니다."
        )
        String password,
        // 비밀번호 재확인
        @NotBlank @Size(min = 8, max = 64)
        String passwordConfirm,
        @NotBlank String phoneRaw,
        AddressDTO address
) {}
