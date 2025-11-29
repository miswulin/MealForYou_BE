package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "회원 마이페이지 API", description = "마이페이지에서 회원 정보 조회/수정, 선호 식단 태그 조회/수정을 제공합니다.")
public class MemberController {

    private final MemberService memberService;

    // 마이페이지 상단 프로필 조회 api
    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 조회",
            description = """
                    마이페이지 상단에서 사용할 회원 정보를 조회합니다.
                    - 이름, 이메일, 전화번호, 기본 주소, 선호 식단 태그를 한 번에 반환합니다.
                    - 전화번호는 서버에 저장된 E.164 형식 그대로 내려주며, 프론트에서 010-XXXX 형식으로 포맷팅해서 사용하면 됩니다.
                    """
    )
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        MemberProfileResponse profile = memberService.getMyProfile(memberDetails);
        return ResponseEntity.ok(profile);
    }

    // 회원정보 (이름, 전화번호) 수정
    @PatchMapping("/me/info")
    @Operation(
            summary = "회원 기본 정보 수정",
            description = """
                    마이페이지 > 회원정보 수정 화면에서 이름과 전화번호를 변경합니다.
                    - 이메일은 변경하지 않습니다.
                    - 전화번호는 서버에서 E.164 형식으로 정규화하여 저장합니다.
                    """
    )
    public ResponseEntity<?> updateMemberInfo(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody @Valid MemberInfoUpdateRequest request
    ) {
        memberService.updateMemberInfo(memberDetails, request);
        return ResponseEntity.ok("회원정보가 수정되었습니다.");
    }

    // 기본 배송지 수정
    @PatchMapping("/me/address")
    @Operation(
            summary = "회원 기본 배송지(주소) 수정",
            description = """
                    마이페이지 > 배송지 관리 화면에서 기본 배송지 정보를 수정합니다.
                    - 우편번호, 도로명 주소, 상세 주소를 모두 전달해야 합니다.
                    - 필요 시 null 을 보내어 기본 주소를 제거할 수도 있습니다.
                    """
    )
    public ResponseEntity<?> updateAddress(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody(required = false) AddressDTO request
    ) {
        memberService.updateAddress(memberDetails, request);
        return ResponseEntity.ok("주소가 수정되었습니다.");
    }

    // 선호 식단 태그 조회
    @GetMapping("/me/health-tags")
    @Operation(
            summary = "선호 식단 태그 조회",
            description = """
                    현재 회원이 선택해 둔 선호 식단 태그 목록을 조회합니다.
                    - 회원가입 시 선택한 태그 또는 마이페이지에서 수정한 태그가 반환됩니다.
                    - 아무 것도 선택하지 않은 경우 빈 배열([])을 반환합니다.
                    """
    )
    public ResponseEntity<HealthTagsResponse> getHealthTags(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        HealthTagsResponse response = memberService.getHealthTags(memberDetails);
        return ResponseEntity.ok(response);
    }

    // 선호 식단 태그 수정
    @PatchMapping("/me/health-tags")
    @Operation(
            summary = "선호 식단 태그 수정",
            description = """
                    회원의 선호 식단 태그 목록을 수정합니다.
                    - 요청 바디의 healthTags 필드는 ProductTag ENUM 값 배열입니다.
                    - 최대 3개까지 선택할 수 있으며, @Size(max=3)로 검증합니다.
                    - null 또는 빈 배열([])을 보내면 '선호 식단 없음' 상태로 저장됩니다.
                    
                    사용 가능한 ENUM 값:
                    - HIGH_PROTEIN (고단백)
                    - LOW_CARB (저탄수)
                    - GLUTEN_FREE (글루텐프리)
                    - LOW_SODIUM (저염)
                    - LOW_GLYCEMIC (저혈당)
                    - VEGAN (비건)
                    """
    )
    public ResponseEntity<?> updateHealthTags(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody @Valid HealthTagsUpdateRequest request
    ) {
        memberService.updateHealthTags(memberDetails, request);
        return ResponseEntity.ok("선호 식단 태그가 수정되었습니다.");
    }
}
