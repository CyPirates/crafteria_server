package com.example.crafteria_server.domain.deliverytracking.service;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.repository.DeliveryRepository;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "TrackingWebhookKeepAlive")
public class TrackingWebhookKeepAlive {

    private final TrackingWebhookRegistrar registrar;
    private final DeliveryRepository deliveryRepository;

    // 매일 새벽 3시 실행(서버 타임존 기준)
    @Scheduled(cron = "0 0 3 * * *")
    public void keepAlive() {
        List<Delivery> targets =
                deliveryRepository.findAllByTrackingNumberNotNullAndOrder_StatusNot(OrderStatus.DELIVERED);

        int updated = 0;
        for (Delivery d : targets) {
            String carrierId = d.getCourier(); // 너희 시스템에서 courier == carrierId 로 사용
            String tracking = d.getTrackingNumber();
            if (carrierId != null && tracking != null) {
                registrar.registerTrackWebhook(carrierId, tracking);
                updated++;
            }
        }
        log.info("[Webhook KeepAlive] 갱신 대상={}건", updated);
    }
}
