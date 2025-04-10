package com.example.crafteria_server.domain.pay.service;

import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.example.crafteria_server.domain.pay.dto.PaymentDto;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@Slf4j(topic = "OrderService")
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${portone_api_secret}")
    private String portoneApiSecret;

    public PaymentDto.PaymentResultDto processPayment(String paymentId, Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + portoneApiSecret);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PaymentDto.PaymentResponse> response = restTemplate.exchange(
                "https://api.portone.io/payments/" + URLEncoder.encode(paymentId, StandardCharsets.UTF_8),
                HttpMethod.GET, entity, PaymentDto.PaymentResponse.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("결제 정보 검색에 실패하였습니다.");
        }

        PaymentDto.PaymentResponse payment = response.getBody();
        if (payment == null || !payment.getAmount().equals(order.getPurchasePrice())) {
            throw new Exception("결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        // 결제 상태에 따른 주문 상태 업데이트
        switch (payment.getStatus()) {
            case "VIRTUAL_ACCOUNT_ISSUED":
                order.setStatus(OrderStatus.IN_PRODUCTING); // 예시로 IN_PRODUCTING 설정, 실제 상황에 맞게 조정 필요
                break;
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                break;
            default:
                throw new Exception("처리되지 않은 결제 상태: " + payment.getStatus());
        }

        orderRepository.save(order);  // 상태 변경 후 저장
        return new PaymentDto.PaymentResultDto(payment.getStatus(), "결제가 성공적으로 처리되었습니다.");
    }
}

