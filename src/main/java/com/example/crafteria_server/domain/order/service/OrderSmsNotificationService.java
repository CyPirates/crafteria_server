package com.example.crafteria_server.domain.order.service;

import com.example.crafteria_server.config.SolapiSmsClient;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSmsNotificationService {

    private final SolapiSmsClient solapiSmsClient;

    public void sendOrderCreatedToManufacturer(Order order) {
        Manufacturer manufacturer = order.getManufacturer();
        if (manufacturer == null) {
            log.warn("[SMS] 제조사가 없는 주문입니다. orderId={}", order.getId());
            return;
        }

        User dashboardUser = manufacturer.getDashboardUser();
        if (dashboardUser == null) {
            log.warn("[SMS] 제조사에 매핑된 대시보드 유저가 없습니다. manufacturerId={}", manufacturer.getId());
            return;
        }

        String to = normalizePhone(dashboardUser.getPhoneNumber());
        if (to == null) {
            log.warn("[SMS] 유효한 전화번호가 없습니다. userId={}, phone={}",
                    dashboardUser.getId(), dashboardUser.getPhoneNumber());
            return;
        }

        String customerName = order.getUser().getRealname() != null
                ? order.getUser().getRealname()
                : order.getUser().getUsername();

        String text = """
                [Crafteria] 새 주문이 접수되었습니다.
                주문ID: %d
                주문자: %s
                결제금액: %d원
                대시보드에서 주문 상세를 확인해 주세요.
                """.formatted(order.getId(), customerName, order.getPurchasePrice());

        solapiSmsClient.sendSimpleMessage(to, text);
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("\\D", "");
        return digits.length() < 10 ? null : digits;
    }
}
