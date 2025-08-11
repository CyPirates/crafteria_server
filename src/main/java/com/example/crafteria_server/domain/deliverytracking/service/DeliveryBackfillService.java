package com.example.crafteria_server.domain.deliverytracking.service;

import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.repository.DeliveryRepository;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "DeliveryBackfillService")
public class DeliveryBackfillService {

    private final DeliveryRepository deliveryRepository;
    private final TrackingWebhookRegistrar webhookRegistrar;

    /**
     * 과거 배송건(운송장 존재 & 주문 미완료)에 대해 Webhook을 일괄 등록
     */
    @Transactional(readOnly = true)
    public int registerAllExistingDeliveriesForWebhook() {
        List<Delivery> targets =
                deliveryRepository.findAllByTrackingNumberNotNullAndOrder_StatusNot(OrderStatus.DELIVERED);

        int count = 0;
        for (Delivery d : targets) {
            String carrierId = d.getCourier();
            String tracking = d.getTrackingNumber();
            if (carrierId == null || carrierId.isBlank() || tracking == null || tracking.isBlank()) {
                continue;
            }
            webhookRegistrar.registerTrackWebhook(carrierId, tracking);
            count++;
        }
        log.info("[Webhook Backfill] 등록 대상: {}건", count);
        return count;
    }
}
