package com.example.crafteria_server.domain.pay.service;

import com.example.crafteria_server.domain.coupon.entity.Coupon;
import com.example.crafteria_server.domain.coupon.service.CouponService;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.entity.ModelPurchase;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.example.crafteria_server.domain.pay.dto.PaymentDto;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;


@Service
@Slf4j(topic = "OrderService")
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final ModelRepository modelRepository;
    private final ModelPurchaseRepository modelPurchaseRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final UserService userService;
    private final CouponService couponService;

    @Value("${portone_api_secret}")
    private String portoneApiSecret;

    // 주문 결제 검증
    public PaymentDto.PaymentResultDto processPayment(String paymentId, Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        PaymentDto.PaymentResponse payment = getPaymentFromPortOne(paymentId);

        // 주문 생성 시 이미 VAT + 배송비 포함된 최종 금액이 저장되어 있으므로, 그대로 비교함
        BigDecimal expectedTotal = BigDecimal.valueOf(order.getPurchasePrice());
        BigDecimal actualPaid = payment.getAmount().getTotal();

        log.info("[결제 금액 확인] 주문ID: {}, 결제ID: {}, 결제금액: {}, 주문총액(검증기준): {}",
                order.getId(), paymentId, actualPaid, expectedTotal);

        if (actualPaid.compareTo(expectedTotal) != 0) {
            throw new Exception("결제 금액이 주문 총액과 일치하지 않습니다.");
        }

        // 결제 상태 처리
        switch (payment.getStatus()) {
            case "VIRTUAL_ACCOUNT_ISSUED":
                order.setStatus(OrderStatus.IN_PRODUCTING);
                break;
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                break;
            default:
                throw new Exception("처리되지 않은 결제 상태: " + payment.getStatus());
        }

        orderRepository.save(order);

        // ✅ 쿠폰 사용 처리
        if (order.getCoupon() != null) {
            couponService.markCouponAsUsed(order.getCoupon().getId(), order.getUser().getId());
        }

        // ✅ 구매자 통계 업데이트
        User user = order.getUser();
        user.setTotalPurchaseCount(user.getTotalPurchaseCount() + 1);
        user.setTotalPurchaseAmount(user.getTotalPurchaseAmount() + order.getPurchasePrice());
        userService.updateUserLevel(user);
        userRepository.save(user);

        // ✅ 판매자 통계 업데이트
        User seller = order.getManufacturer().getDashboardUser();
        seller.setTotalPrintedCount(seller.getTotalPrintedCount() + 1);
        seller.setTotalPrintedAmount(seller.getTotalPrintedAmount() + order.getPurchasePrice());
        userService.updateUserLevel(seller);
        userRepository.save(seller);

        log.info("[결제 완료] 주문ID: {}, 결제ID: {}, 최종상태: {}", order.getId(), paymentId, order.getStatus());

        return new PaymentDto.PaymentResultDto(payment.getStatus(), "결제가 성공적으로 처리되었습니다.");
    }

    // 모델 결제 검증
    public PaymentDto.PaymentResultDto processModelPayment(String paymentId, Long modelId, Long userId) throws Exception {
        PaymentDto.PaymentResponse payment = getPaymentFromPortOne(paymentId);

        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("모델을 찾을 수 없습니다."));

        ModelPurchase purchase = modelPurchaseRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 내역과 일치하는 구매 기록이 없습니다."));

        if (purchase.isVerified()) {
            throw new Exception("이미 검증 완료된 결제입니다.");
        }

        // ✅ 쿠폰 할인 고려한 금액 계산
        int originalPrice = (int) model.getPrice();
        int discount = 0;
        Coupon coupon = purchase.getCoupon();
        if (coupon != null) {
            discount = (originalPrice * coupon.getDiscountRate()) / 100;
            discount = Math.min(discount, coupon.getMaxDiscountAmount());
        }

        int discountedPrice = originalPrice - discount;
        int vat = (int) Math.ceil(discountedPrice * 0.1);
        int expectedTotal = discountedPrice + vat;

        if (payment.getAmount().getTotal().intValue() != expectedTotal) {
            throw new Exception("결제 금액이 모델 가격(VAT 포함)과 일치하지 않습니다.");
        }

        // 결제 검증 성공 처리
        purchase.setVerified(true);
        modelPurchaseRepository.save(purchase);

        // ✅ 다운로드 수 증가
        model.setDownloadCount(model.getDownloadCount() + 1);
        modelRepository.save(model);

        // ✅ 쿠폰 사용 처리
        if (coupon != null) {
            couponService.markCouponAsUsed(coupon.getId(), userId);
        }

        // ✅ 구매자 통계 처리
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        buyer.setTotalPurchaseCount(buyer.getTotalPurchaseCount() + 1);
        buyer.setTotalPurchaseAmount(buyer.getTotalPurchaseAmount() + discountedPrice);
        userService.updateUserLevel(buyer);
        userRepository.save(buyer);

        // ✅ 판매자 통계 처리
        User seller = model.getAuthor().getUser();
        seller.setTotalSalesCount(seller.getTotalSalesCount() + 1);
        seller.setTotalSalesAmount(seller.getTotalSalesAmount() + discountedPrice);
        userService.updateUserLevel(seller);
        userRepository.save(seller);

        log.info("[결제 검증 완료] 모델ID: {}, 구매자ID: {}, 결제ID: {}, 최종금액: {}", modelId, userId, paymentId, expectedTotal);

        return new PaymentDto.PaymentResultDto(payment.getStatus(), "모델 결제가 성공적으로 검증 및 처리되었습니다.");
    }

    // ✳️ 할인 + VAT 포함 최종가격 계산 유틸
    private int calculateModelFinalPrice(Model model, Coupon coupon) {
        int originalPrice = (int) model.getPrice();
        int discount = 0;

        if (coupon != null) {
            discount = (originalPrice * coupon.getDiscountRate()) / 100;
            discount = Math.min(discount, coupon.getMaxDiscountAmount());
        }

        int discountedPrice = originalPrice - discount;
        int vat = (int) Math.ceil(discountedPrice * 0.1);
        return discountedPrice + vat;
    }

    private PaymentDto.PaymentResponse getPaymentFromPortOne(String paymentId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + portoneApiSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 1. 원시 JSON 로그 출력 (유지)
        ResponseEntity<String> raw = restTemplate.exchange(
                "https://api.portone.io/payments/" + URLEncoder.encode(paymentId, StandardCharsets.UTF_8),
                HttpMethod.GET, entity, String.class
        );
        log.warn("📦 PortOne 응답 원문: {}", raw.getBody());

        // 2. DTO 직접 매핑 (중간에 response 필드 없음)
        ResponseEntity<PaymentDto.PaymentResponse> response = restTemplate.exchange(
                "https://api.portone.io/payments/" + URLEncoder.encode(paymentId, StandardCharsets.UTF_8),
                HttpMethod.GET, entity, PaymentDto.PaymentResponse.class
        );

        PaymentDto.PaymentResponse payment = response.getBody();
        if (payment == null || payment.getAmount() == null) {
            log.warn("❗ PortOne 응답에서 결제 정보가 없습니다: {}", response);
            throw new Exception("PortOne 응답에서 결제 정보가 누락되었습니다.");
        }

        return payment;
    }
}

