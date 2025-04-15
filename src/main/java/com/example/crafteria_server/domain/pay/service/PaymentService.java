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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


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
        if (!payment.getAmount().getTotal().equals(BigDecimal.valueOf(order.getPurchasePrice()))) {
            throw new Exception("결제 금액이 주문 금액과 일치하지 않습니다.");
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
        return new PaymentDto.PaymentResultDto(payment.getStatus(), "결제가 성공적으로 처리되었습니다.");
    }

    // 모델 결제 검증
    public PaymentDto.PaymentResultDto processModelPayment(String paymentId, Long modelId, Long userId) throws Exception {
        if (modelPurchaseRepository.existsByPaymentId(paymentId)) {
            throw new Exception("이미 처리된 결제입니다.");
        }

        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeException("모델을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        PaymentDto.PaymentResponse payment = getPaymentFromPortOne(paymentId);
        if (!payment.getAmount().getTotal().equals(BigDecimal.valueOf(model.getPrice()))) {
            throw new Exception("결제 금액이 모델 가격과 일치하지 않습니다.");
        }

        ModelPurchase purchase = ModelPurchase.builder()
                .model(model)
                .user(user)
                .paymentId(paymentId)
                .build();
        modelPurchaseRepository.save(purchase);

        return new PaymentDto.PaymentResultDto(payment.getStatus(), "모델 결제가 성공적으로 처리되었습니다.");
    }

    // 포트원 API 호출 및 유효성 검증
    private PaymentDto.PaymentResponse getPaymentFromPortOne(String paymentId) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + portoneApiSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PaymentDto.PortOneResponseWrapper> response = restTemplate.exchange(
                "https://api.portone.io/payments/" + URLEncoder.encode(paymentId, StandardCharsets.UTF_8),
                HttpMethod.GET, entity, PaymentDto.PortOneResponseWrapper.class
        );

        PaymentDto.PortOneResponseWrapper body = response.getBody();
        if (body == null) {
            throw new Exception("PortOne 응답이 비어 있습니다.");
        }

        if (body.getCode() != 0) {
            throw new Exception("PortOne 응답 에러: " + body.getMessage());
        }

        if (body.getResponse() == null || body.getResponse().getAmount() == null) {
            throw new Exception("PortOne 응답에서 결제 정보가 누락되었습니다.");
        }

        return body.getResponse();
    }
}

