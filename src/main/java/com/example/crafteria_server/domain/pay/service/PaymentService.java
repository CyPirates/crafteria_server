package com.example.crafteria_server.domain.pay.service;

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

    @Value("${portone_api_secret}")
    private String portoneApiSecret;

    // 주문 결제 검증
    public PaymentDto.PaymentResultDto processPayment(String paymentId, Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        PaymentDto.PaymentResponse payment = getPaymentFromPortOne(paymentId);

        log.info("[결제 금액] 주문ID: {}, 결제ID: {}, 결제금액: {}",
                order.getId(), paymentId, payment.getAmount().getTotal());



        BigDecimal basePrice = BigDecimal.valueOf(order.getPurchasePrice());
        BigDecimal vat = basePrice.multiply(BigDecimal.valueOf(0.10)).setScale(0, RoundingMode.CEILING);
        BigDecimal deliveryFee = BigDecimal.valueOf(3000);
        BigDecimal expectedTotal = basePrice.add(vat).add(deliveryFee);

        if (payment.getAmount().getTotal().compareTo(expectedTotal) != 0) {
            throw new Exception("결제 금액이 주문 총액(VAT + 배송비 포함)과 일치하지 않습니다.");
        }

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

        log.info("[결제 완료] 주문ID: {}, 결제ID: {}, 최종상태: {}", order.getId(), paymentId, order.getStatus());

        return new PaymentDto.PaymentResultDto(payment.getStatus(), "결제가 성공적으로 처리되었습니다.");
    }

    // 모델 결제 검증
    public PaymentDto.PaymentResultDto processModelPayment(String paymentId, Long modelId, Long userId) throws Exception {
        PaymentDto.PaymentResponse payment = getPaymentFromPortOne(paymentId); // ✅ 포트원 API로 결제 내역 조회

        // 유효하지 않은 결제 ID이면 여기서 Exception 발생

        // 모델 가격 확인
        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("모델을 찾을 수 없습니다."));

        log.info("[모델 결제 금액] 모델ID: {}, 결제ID: {}, 결제금액: {}",
                model.getId(), paymentId, payment.getAmount().getTotal());

        BigDecimal modelPrice = BigDecimal.valueOf(model.getPrice());
        BigDecimal vat = modelPrice.multiply(BigDecimal.valueOf(0.10)).setScale(0, RoundingMode.CEILING);
        BigDecimal expectedTotal = modelPrice.add(vat);

        if (payment.getAmount().getTotal().compareTo(expectedTotal) != 0) {
            throw new Exception("결제 금액이 모델 가격(VAT 포함)과 일치하지 않습니다.");
        }

        Optional<ModelPurchase> existingPurchaseOpt = modelPurchaseRepository.findByPaymentId(paymentId);
        if (existingPurchaseOpt.isPresent()) {
            ModelPurchase existingPurchase = existingPurchaseOpt.get();
            if (existingPurchase.isVerified()) {
                throw new Exception("이미 검증 완료된 결제입니다.");
            }

            // ✅ 검증 성공한 경우에만 verified true로 변경
            existingPurchase.setVerified(true);
            modelPurchaseRepository.save(existingPurchase);

            return new PaymentDto.PaymentResultDto("VERIFIED", "기존 결제 검증 완료 처리되었습니다.");
        }

        // 없는 결제건이면 새로 생성
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        ModelPurchase purchase = ModelPurchase.builder()
                .user(user)
                .model(model)
                .paymentId(paymentId)
                .verified(true)
                .build();

        modelPurchaseRepository.save(purchase);
        return new PaymentDto.PaymentResultDto(payment.getStatus(), "모델 결제가 성공적으로 처리되었습니다.");
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

