package com.example.crafteria_server.domain.pay.controller;

import com.example.crafteria_server.domain.pay.dto.PaymentDto;
import com.example.crafteria_server.domain.pay.service.PaymentService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "PaymentController")
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private PaymentService paymentService;

    @PostMapping("/complete")    
    public JsonBody<PaymentDto.PaymentResultDto> completePayment(@RequestBody PaymentDto.PaymentRequestDto paymentRequest) {
        try {
            PaymentDto.PaymentResultDto paymentResult = paymentService.processPayment(paymentRequest.getPaymentId(), paymentRequest.getOrder());
            return JsonBody.of(200, "성공", paymentResult);
        } catch (Exception e) {
            return JsonBody.of(400, "실패: " + e.getMessage(), null);
        }
    }

    @PostMapping("/model/complete")
    public JsonBody<PaymentDto.PaymentResultDto> completeModelPayment(@RequestBody PaymentDto.ModelPaymentRequestDto paymentRequest,
                                                                      @AuthenticationPrincipal PrincipalDetails principalDetails) {
        try {
            PaymentDto.PaymentResultDto paymentResult = paymentService.processModelPayment(
                    paymentRequest.getPaymentId(),
                    paymentRequest.getModelId(),
                    principalDetails.getUser().getId()
            );
            return JsonBody.of(200, "성공", paymentResult);
        } catch (Exception e) {
            return JsonBody.of(400, "실패: " + e.getMessage(), null);
        }
    }
}