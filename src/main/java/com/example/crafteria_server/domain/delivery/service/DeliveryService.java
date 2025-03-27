package com.example.crafteria_server.domain.delivery.service;

import com.example.crafteria_server.domain.delivery.dto.DeliveryDto;
import com.example.crafteria_server.domain.delivery.entity.Delivery;
import com.example.crafteria_server.domain.delivery.repository.DeliveryRepository;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.example.crafteria_server.domain.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    private void checkOrderStatusForDelivery(Long orderId) throws AccessDeniedException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getStatus().equals(OrderStatus.PRODUCTED)) {
            throw new AccessDeniedException("Delivery functionality is available only when order status is 'PRODUCTED'.");
        }
    }

    public DeliveryDto.DeliveryResponse createDelivery(DeliveryDto.DeliveryRequest request, User user) throws AccessDeniedException {
        checkOrderStatusForDelivery(request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getManufacturer().getId().equals(user.getManufacturer().getId())) {
            throw new AccessDeniedException("You do not have permission to access this order.");
        }

        Delivery delivery = Delivery.builder()
                .order(order)
                .courier(request.getCourier())
                .trackingNumber(request.getTrackingNumber())
                .build();

        delivery = deliveryRepository.save(delivery);
        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    public DeliveryDto.DeliveryResponse updateDelivery(Long deliveryId, DeliveryDto.DeliveryRequest request, User user) throws AccessDeniedException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found"));

        checkOrderStatusForDelivery(delivery.getOrder().getId());

        if (!delivery.getOrder().getManufacturer().getId().equals(user.getManufacturer().getId())) {
            throw new AccessDeniedException("You do not have permission to modify this delivery.");
        }

        delivery.setCourier(request.getCourier());
        delivery.setTrackingNumber(request.getTrackingNumber());
        delivery = deliveryRepository.save(delivery);

        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    public void deleteDelivery(Long deliveryId, User user) throws AccessDeniedException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found"));

        checkOrderStatusForDelivery(delivery.getOrder().getId());

        if (!delivery.getOrder().getManufacturer().getId().equals(user.getManufacturer().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this delivery.");
        }

        deliveryRepository.delete(delivery);
    }
}