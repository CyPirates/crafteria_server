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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;

    private void validateOrderStatusForCreate(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getStatus().equals(OrderStatus.PRODUCTED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배송 등록은 주문 상태가 'PRODUCTED'일 때만 가능합니다.");
        }
    }

    private void validateOrderStatusForUpdateOrDelete(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!(order.getStatus().equals(OrderStatus.PRODUCTED) || order.getStatus().equals(OrderStatus.DELIVERING))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배송 수정/삭제는 'PRODUCTED' 또는 'DELIVERING' 상태에서만 가능합니다.");
        }
    }

    public DeliveryDto.DeliveryResponse createDelivery(DeliveryDto.DeliveryRequest request, User user) throws AccessDeniedException {
        validateOrderStatusForCreate(request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getManufacturer().getId().equals(user.getManufacturer().getId())) {
            throw new AccessDeniedException("You do not have permission to access this order.");
        }

        order.setStatus(OrderStatus.DELIVERING);
        orderRepository.save(order);

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

        validateOrderStatusForUpdateOrDelete(delivery.getOrder().getId());

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

        validateOrderStatusForUpdateOrDelete(delivery.getOrder().getId());

        if (!delivery.getOrder().getManufacturer().getId().equals(user.getManufacturer().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this delivery.");
        }

        deliveryRepository.delete(delivery);
    }

    public DeliveryDto.DeliveryResponse getDeliveryById(Long deliveryId, User user) throws AccessDeniedException {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery not found"));

        Long userManufacturerId = user.getManufacturer().getId();
        Long deliveryManufacturerId = delivery.getOrder().getManufacturer().getId();

        if (!userManufacturerId.equals(deliveryManufacturerId)) {
            throw new AccessDeniedException("You do not have permission to view this delivery.");
        }

        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    public List<DeliveryDto.DeliveryResponse> getMyDeliveries(User user) {
        Long manufacturerId = user.getManufacturer().getId();
        List<Delivery> deliveries = deliveryRepository.findAllByOrder_Manufacturer_Id(manufacturerId);
        return deliveries.stream()
                .map(DeliveryDto.DeliveryResponse::from)
                .collect(Collectors.toList());
    }
}