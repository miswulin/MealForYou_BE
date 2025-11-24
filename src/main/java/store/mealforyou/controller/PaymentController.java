package store.mealforyou.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store.mealforyou.dto.PaymentRequestDto;
import store.mealforyou.security.MemberDetails;
import store.mealforyou.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "포트원 결제 검증 및 주문 완료 처리를 담당합니다.")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/complete")
    @Operation(
            summary = "결제 검증 및 주문 완료",
            description = "프론트엔드에서 결제 완료 후 호출합니다. 서버에서 금액을 검증한 후 주문을 생성합니다."
    )
    public ResponseEntity<Long> completePayment(
            @AuthenticationPrincipal MemberDetails user,
            @RequestBody PaymentRequestDto request) {

        // 검증 및 주문 생성 후 주문 ID 반환
        Long orderId = paymentService.verifyAndPlaceOrder(user.id(), request);
        return ResponseEntity.ok(orderId);
    }
}