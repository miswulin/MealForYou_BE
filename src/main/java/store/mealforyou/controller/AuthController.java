package store.mealforyou.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import store.mealforyou.dto.*;
import store.mealforyou.service.AuthService;
import store.mealforyou.service.EmailAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService; // 회원가입/로그인/토큰 재발급
    private final EmailAuthService emailAuthService; // 이메일 인증코드 발송/검

    // 이메일 인증코드 발송 (재발송 포함)
    @PostMapping("/email/send")
    public ResponseEntity<?> sendEmailCode(@RequestBody @Valid SendCodeRequest request) {
        emailAuthService.sendCode(request.email());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    // 이메일 인증코드 검증
    @PostMapping("/email/verify")
    public ResponseEntity<?> verifyEmailCode(@RequestBody @Valid VerifyCodeRequest request) {
        emailAuthService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        Long id = authService.signup(request);
        return ResponseEntity.ok("회원가입이 성공 (id=" + id + ")");
    }

    // 로그인 (Access + Refresh Token 반환)
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ResponseEntity.ok(tokens);
    }

    // Access Token 재발급 (refreshToken 하나만 받고, 서비스 계층에서 토큰 내부에서 email을 추출함)
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam String refreshToken) {
        TokenResponse tokens = authService.refresh(refreshToken);
        return ResponseEntity.ok(tokens);
    }

    // 로그아웃 (refreshToken 하나만 보내면 되고, refreshToken 내부 email 추출 후 Redis에서 제거함)
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        String email = authService.extractEmail(refreshToken);
        authService.logout(email);
        return ResponseEntity.ok("로그아웃되었습니다.");
    }
}
