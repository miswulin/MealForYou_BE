package store.mealforyou.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.mealforyou.dto.PrivatePasswordChangeRequest;
import store.mealforyou.dto.PublicPasswordResetRequest;
import store.mealforyou.entity.Member;
import store.mealforyou.security.MemberDetailService;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.PasswordService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/password")
public class PasswordController {

    private final PasswordService passwordService;

    // 비로그인 사용자의 비밀번호 재설정
    @PostMapping("/reset")
    public ResponseEntity<?> reset(@Valid @RequestBody PublicPasswordResetRequest request) {

        passwordService.resetPassword(request);

        return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다.");
    }

    // 로그인한 사용자의 비밀번호 변경
    @PostMapping("/change")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @RequestBody PrivatePasswordChangeRequest request
    ) {
        passwordService.changePassword(memberDetails, request);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
