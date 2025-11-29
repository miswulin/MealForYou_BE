package store.mealforyou.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import store.mealforyou.constant.ProductTag;

import java.util.List;

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
        AddressDTO address,

        // 회원이 선택한 선호 식단 태그 목록
        // null 또는 빈 리스트면 "선호 식단 없음"으로 처리
        // 최대 3개까지 허용
        @Size(max = 3, message = "선호 식단은 최대 3가지까지 선택할 수 있습니다.")
        List<ProductTag> healthTags
) {}
