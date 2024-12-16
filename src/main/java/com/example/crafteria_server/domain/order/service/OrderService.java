package com.example.crafteria_server.domain.order.service;


import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.order.dto.OrderDto;
import com.example.crafteria_server.domain.order.entity.Order;
import com.example.crafteria_server.domain.order.entity.OrderItem;
import com.example.crafteria_server.domain.order.entity.OrderStatus;
import com.example.crafteria_server.domain.order.repository.OrderRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j(topic = "OrderService")
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ModelPurchaseRepository modelPurchaseRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ManufacturerRepository manufacturerRepository;  // 추가

    public List<OrderDto.OrderResponse> getMyOrderList(Long userId, int page) {
        PageRequest pageable = PageRequest.of(page, 10);
        return orderRepository.findAllByUserId(userId, pageable).stream()
                .map(OrderDto.OrderResponse::from)
                .toList();
    }

    public OrderDto.OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(userId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        return OrderDto.OrderResponse.from(order);
    }

    public OrderDto.OrderResponse createOrder(Long userId, OrderDto.OrderRequest request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 제조사 조회
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조사를 찾을 수 없습니다."));

        // 주문 객체 생성
        Order order = Order.builder()
                .user(user)
                .manufacturer(manufacturer)
                .deliveryAddress(request.getDeliveryAddress())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .recipientEmail(request.getRecipientEmail())
                .specialRequest(request.getSpecialRequest())
                .purchasePrice(request.getPurchasePrice())
                .status(getOrderStatusFromKey(request.getStatus()))
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        // 각 주문 아이템에 대해 처리
        for (OrderDto.OrderItemDto itemDto : request.getOrderItems()) {
            // 파일 저장 로직 구현(여기서는 예제로 간단하게 처리)
            File file = fileService.saveModel(itemDto.getModelFile());

            // 주문 아이템 객체 생성
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .file(file)
                    .widthSize(itemDto.getWidthSize())
                    .lengthSize(itemDto.getLengthSize())
                    .heightSize(itemDto.getHeightSize())
                    .magnification(itemDto.getMagnification())
                    .quantity(itemDto.getQuantity())
                    .build();

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems); // 주문 아이템 리스트 설정
        orderRepository.save(order); // 주문 저장

        return OrderDto.OrderResponse.from(order); // 주문 응답 생성 및 반환
    }


    public OrderDto.OrderResponse cancelOrderByUser(Long userId, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(userId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (!order.getStatus().equals(OrderStatus.ORDERED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소할 수 없는 상태입니다.");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        return OrderDto.OrderResponse.from(orderRepository.save(order));
    }

    public OrderDto.OrderResponse changeOrderStatusByManufacturer(Long manufacturerId, Long orderId, String newStatusKey) {
        Order order = orderRepository.findByManufacturerIdAndId(manufacturerId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getStatus().equals(OrderStatus.CANCELED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다.");
        }

        OrderStatus newStatus = getOrderStatusFromKey(newStatusKey);

        order.setStatus(newStatus);
        orderRepository.save(order);
        log.info("변경된 주문 상태: {}", newStatusKey);

        return OrderDto.OrderResponse.from(orderRepository.save(order));
    }

    private OrderStatus getOrderStatusFromKey(String key) {
        return Arrays.stream(OrderStatus.values())
                .filter(status -> status.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 상태입니다."));
    }

    public List<OrderDto.OrderResponse> getOrderedOrdersByManufacturer(Long manufacturerId) {
        List<Order> orders = orderRepository.findByManufacturerIdAndStatus(manufacturerId, OrderStatus.ORDERED);

        // 주문 엔티티를 DTO로 변환하여 반환
        return orders.stream()
                .map(OrderDto.OrderResponse::from)
                .toList();
    }

    public List<OrderDto.OrderResponse> getAllOrdersByManufacturer(Long manufacturerId) {
        List<Order> orders = orderRepository.findByManufacturerId(manufacturerId);

        // 주문 엔티티를 DTO로 변환하여 반환
        return orders.stream()
                .map(OrderDto.OrderResponse::from)
                .toList();
    }
}
