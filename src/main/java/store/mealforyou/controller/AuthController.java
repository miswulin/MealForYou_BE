package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.AuthService;
import store.mealforyou.service.EmailAuthService;
import store.mealforyou.service.PasswordService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "인증/회원 관련 API", description = "이메일 인증, 회원가입, 로그인, 토큰 재발급, 로그아웃 기능을 제공합니다.")
public class AuthController {

    private final AuthService authService; // 회원가입/로그인/토큰 재발급
    private final EmailAuthService emailAuthService; // 이메일 인증코드 발송/검

    // 이메일 인증코드 발송 (재발송 포함)
    @PostMapping("/email/send")
    @Operation(
            summary = "이메일 인증코드 발송",
            description = "회원가입 전에 이메일 소유자를 확인하기 위해 4자리 인증코드를 발송합니다." +
                    "\n개발 환경에서는 편의성을 위해 응답 메시지에 devCode를 포함했습니다."
    )
    public ResponseEntity<?> sendEmailCode(@RequestBody @Valid SendCodeRequest request) {
        String code = emailAuthService.sendCode(request.email());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다. (devCode=" + code + ")");
    }

    // 이메일 인증코드 검증
    @PostMapping("/email/verify")
    @Operation(
            summary = "이메일 인증코드 검증",
            description = "사용자가 입력한 4자리 인증코드가 Redis 서버에 저장된 값과 일치하는지 확인하고, 성공 시 24시간 유효한 인증 플래그를 저장합니다."
    )
    public ResponseEntity<?> verifyEmailCode(@RequestBody @Valid VerifyCodeRequest request) {
        emailAuthService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    // 회원가입
    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description =
                    "이메일 인증이 완료된 사용자가 가입을 위한 정보를 입력하여 회원가입을 수행합니다.\n\n" +
                            "- 필수 정보: 이메일, 이름, 비밀번호, 전화번호, 주소(선택)\n" +
                            "- 건강 선호 식단(선택): 최대 3가지까지 선택 가능하며, " +
                            "다음 ENUM 값을 사용합니다.\n" +
                            "  * HIGH_PROTEIN (고단백)\n" +
                            "  * LOW_CARB (저탄수)\n" +
                            "  * GLUTEN_FREE (글루텐프리)\n" +
                            "  * LOW_SODIUM (저염)\n" +
                            "  * LOW_GLYCEMIC (저혈당)\n" +
                            "  * VEGAN (비건)\n\n" +
                            "요청 바디에서 healthTags 필드를 생략하거나 빈 배열([])로 보내면 " +
                            "선호 식단을 선택하지 않은 것으로 처리됩니다."
    )
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        Long id = authService.signup(request);
        return ResponseEntity.ok("회원가입이 성공 (id=" + id + ")");
    }

    // 로그인 (Access + Refresh Token 반환)
    @PostMapping("/login")
    @Operation(
            summary = "로그인 및 JWT 토큰 발급",
            description = "이메일과 비밀번호로 인증을 수행하고, Access Token과 Refresh Token을 발급합니다. Refresh Token은 Redis 서버에 저장됩니다."
    )
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    // Access Token 재발급 (refreshToken 하나만 받고, 서비스 계층에서 토큰 내부에서 email을 추출함)
    @PostMapping("/refresh")
    @Operation(
            summary = "Access Token 재발급",
            description = "유효한 Refresh Token을 이용하여 새 Access Token과 새 Refresh Token을 발급합니다."
    )
    public TokenResponse refresh(@RequestBody @Validated RefreshRequest request) {
        return authService.refresh(request);
    }

    // 로그아웃 (refreshToken 하나만 보내면 되고, refreshToken 내부 email 추출 후 Redis에서 제거함)
    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "현재 로그인한 사용자의 Refresh Token을 Redies 서버에서 삭제하여 재발급을 차단합니다."
    )
    public ResponseEntity<?> logout(@AuthenticationPrincipal MemberDetails memberDetails,
                                    @RequestBody LogoutRequest request
    ) {
        // SecurityContext에서 꺼낸 이메일
        String email = memberDetails.email();

        // 이메일 + Refresh 토큰을 함께 검증+처리
        authService.logout(email, request.refreshToken()); // Redis에서 refresh:{email} 삭제
        return ResponseEntity.ok("로그아웃되었습니다.");
    }

    // 회원 탈퇴
    @PostMapping("/delete")
    @Operation(
            summary = "회원탈퇴",
            description = "현재 로그인한 사용자의 계정 정보를 삭제합니다."
    )
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        String email = memberDetails.email();
        authService.deleteAccount(email);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
