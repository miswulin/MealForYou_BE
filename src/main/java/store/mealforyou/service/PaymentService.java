package store.mealforyou.service;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.mealforyou.dto.OrderPlaceDto;
import store.mealforyou.dto.PaymentRequestDto;
import store.mealforyou.entity.CartItem;
import store.mealforyou.repository.CartItemRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final IamportClient iamportClient;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    @Transactional
    public Long verifyAndPlaceOrder(Long memberId, PaymentRequestDto request) {
        // 0. 요청 수신 로그
        log.info("🧾 [결제 검증 시작] memberId={}, impUid={}, merchantUid={}, cartItemIds={}",
                memberId, request.getImpUid(), request.getMerchantUid(), request.getCartItemIds());
        try {
            // 포트원 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(request.getImpUid());
            Payment payment = iamportResponse.getResponse();

            if (payment == null) {
                log.warn("❌ [결제 검증 실패] 포트원에서 결제 정보를 찾지 못했습니다. impUid={}", request.getImpUid());
                throw new IllegalArgumentException("결제 내역이 존재하지 않습니다.");
            }

            log.info("📡 [포트원 응답] status={}, payMethod={}, merchantUid={}, amount={}",
                    payment.getStatus(), payment.getPayMethod(),
                    payment.getMerchantUid(), payment.getAmount());

            // merchantUid 검증
            if (!payment.getMerchantUid().equals(request.getMerchantUid())) {
                log.warn("❌ [결제 검증 실패] merchantUid 불일치. 요청={}, 포트원={}",
                        request.getMerchantUid(), payment.getMerchantUid());
                throw new IllegalArgumentException("주문 번호가 일치하지 않습니다.");
            }

            // 결제 상태 확인
            String status = payment.getStatus();

            // paid(결제 완료) 또는 ready(가상 계좌)일 때만 통과
            boolean isPaid = "paid".equals(status);
            boolean isVbankReady = "ready".equals(status) && "vbank".equals(payment.getPayMethod());

            if (!isPaid && !isVbankReady) {
                log.warn("❌ [결제 검증 실패] 유효하지 않은 결제 상태. status={}, payMethod={}",
                        status, payment.getPayMethod());
                throw new IllegalArgumentException("결제가 완료되지 않았거나 유효하지 않습니다. 상태: " + status);
            }

            // 장바구니 조회 및 총액 계산
            List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
            if (cartItems.isEmpty()) {
                log.warn("❌ [결제 검증 실패] 장바구니에 해당 상품이 없습니다. cartItemIds={}",
                        request.getCartItemIds());
                throw new IllegalArgumentException("주문할 상품이 장바구니에 존재하지 않습니다.");
            }

            int dbTotalAmount = cartItems.stream()
                    .mapToInt(c -> c.getPrice() * c.getQuantity())
                    .sum();

            int shippingFee = (dbTotalAmount > 0) ? 2500 : 0;
            int finalExpectedAmount = dbTotalAmount + shippingFee;

            log.info("💰 [금액 검증] dbTotalAmount={}, shippingFee={}, finalExpectedAmount={}, paymentAmount={}",
                    dbTotalAmount, shippingFee, finalExpectedAmount, payment.getAmount());


            // 금액 검증
            if (payment.getAmount().compareTo(BigDecimal.valueOf(finalExpectedAmount)) != 0) {
                // 금액 불일치 시 자동 환불
                if (isPaid) {
                    cancelPayment(payment.getImpUid());
                }
                log.warn("❌ [결제 금액 불일치] paymentAmount={}, expected={}",
                        payment.getAmount(), finalExpectedAmount);
                throw new IllegalArgumentException(
                        "결제 금액 검증 중 오류가 발생했습니다. (결제 금액: "
                                + payment.getAmount() + ", 예상 금액: " + finalExpectedAmount + ")"
                );
            }

            // 주문 생성
            OrderPlaceDto orderPlaceDto = new OrderPlaceDto();
            orderPlaceDto.setCartItemIds(request.getCartItemIds());
            orderPlaceDto.setPaymentType(request.getPaymentType());

            // 주문 생성 시작 로그
            log.info("💳 [결제 검증 완료] 주문 생성 시작: memberId={}, cartItemIds={}, paymentType={}",
                    memberId, request.getCartItemIds(), request.getPaymentType());

            Long orderId = orderService.placeOrder(memberId, orderPlaceDto);

            // 주문 생성 성공 로그
            log.info("✅ [주문 생성 완료] orderId={}", orderId);

            return orderId;
        } catch (IamportResponseException | IOException e) {
            log.error("🚨 [포트원 API 오류] impUid={}, msg={}", request.getImpUid(), e.getMessage(), e);
            throw new RuntimeException("포트원 API 연결 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    // 결제 취소
    private void cancelPayment(String impUid) {
        try {
            CancelData cancelData = new CancelData(impUid, true);

            iamportClient.cancelPaymentByImpUid(cancelData);

            log.info("↩️ [자동 환불 완료] impUid={}", impUid);
        } catch (Exception e) {
            log.error("🚨 [자동 환불 실패] impUid={}, error={}", impUid, e.getMessage(), e);
        }
    }
}