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
@Slf4j(topic = "DeliveryStatusReconciler")
public class DeliveryStatusReconciler {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryTrackingService deliveryTrackingService;

    /**
     * 매 시간 정각마다 미완료 배송 상태 보정
     * (필요시 크론 조정: 매 30분 등)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void reconcileUndelivered() {
        List<Delivery> targets =
                deliveryRepository.findAllByTrackingNumberNotNullAndOrder_StatusNot(OrderStatus.DELIVERED);

        int processed = 0;
        for (Delivery d : targets) {
            String carrierId = d.getCourier();
            String tracking = d.getTrackingNumber();
            if (carrierId == null || carrierId.isBlank() || tracking == null || tracking.isBlank()) {
                continue;
            }
            // 내부에서 GraphQL 호출 → lastEvent.status.code 기반으로 상태 업데이트
            deliveryTrackingService.handleTrackingStatusChange(carrierId, tracking);
            processed++;
        }
        log.info("[Delivery Reconcile] 보정 시도: {}건", processed);
    }
}
