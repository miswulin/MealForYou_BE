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
        try {
            // 포트원 결제 정보 조회
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(request.getImpUid());
            Payment payment = iamportResponse.getResponse();

            if (payment == null) {
                throw new IllegalArgumentException("결제 내역이 존재하지 않습니다.");
            }

            // merchantUid 검증
            if (!payment.getMerchantUid().equals(request.getMerchantUid())) {
                throw new IllegalArgumentException("주문 번호가 일치하지 않습니다.");
            }

            // 결제 상태 확인
            String status = payment.getStatus();

            // paid(결제 완료) 또는 ready(가상 계좌)일 때만 통과
            boolean isPaid = "paid".equals(status);
            boolean isVbankReady = "ready".equals(status) && "vbank".equals(payment.getPayMethod());

            if (!isPaid && !isVbankReady) {
                throw new IllegalArgumentException("결제가 완료되지 않았거나 유효하지 않습니다. 상태: " + status);
            }

            // 장바구니 조회 및 총액 계산
            List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("주문할 상품이 장바구니에 존재하지 않습니다.");
            }

            int dbTotalAmount = cartItems.stream()
                    .mapToInt(c -> c.getPrice() * c.getQuantity())
                    .sum();

            int shippingFee = (dbTotalAmount > 0) ? 2500 : 0;
            int finalExpectedAmount = dbTotalAmount + shippingFee;

            // 금액 검증
            if (payment.getAmount().compareTo(BigDecimal.valueOf(finalExpectedAmount)) != 0) {
                // 금액 불일치 시 자동 환불
                if (isPaid) {
                    cancelPayment(payment.getImpUid());
                }
                throw new IllegalArgumentException(
                        "결제 금액 검증 중 오류가 발생했습니다. (결제 금액: "
                                + payment.getAmount() + ", 예상 금액: " + finalExpectedAmount + ")"
                );
            }

            // 주문 생성
            OrderPlaceDto orderPlaceDto = new OrderPlaceDto();
            orderPlaceDto.setCartItemIds(request.getCartItemIds());
            orderPlaceDto.setPaymentType(request.getPaymentType());

            return orderService.placeOrder(memberId, orderPlaceDto);
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("포트원 API 연결 중 오류가 발생했습니다. " + e.getMessage());
        }
    }

    // 결제 취소
    private void cancelPayment(String impUid) {
        try {
            CancelData cancelData = new CancelData(impUid, true);

            iamportClient.cancelPaymentByImpUid(cancelData);

            log.info("자동 환불 완료: impUid={}, reason={}", impUid, "금액 불일치 자동 환불");
        } catch (Exception e) {
            log.error("자동 환불 실패: impUid={}, reason={}, error={}", impUid, "금액 불일치 자동 환불", e.getMessage());
        }
    }
}