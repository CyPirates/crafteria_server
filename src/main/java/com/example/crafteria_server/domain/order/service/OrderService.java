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
import com.example.crafteria_server.domain.technology.entity.Technology;
import com.example.crafteria_server.domain.technology.repository.TechnologyRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import com.example.crafteria_server.domain.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final TechnologyRepository technologyRepository;  // 추가
    private final UserService userService;

    public List<OrderDto.OrderResponse> getMyOrderList(Long userId, int page) {
        PageRequest pageable = PageRequest.of(page, 10);
        List<Order> orders = orderRepository.findAllByUserIdExcludingOrdered(userId, pageable);
        return orders.stream()
                .map(OrderDto.OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderDto.OrderResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(userId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        return OrderDto.OrderResponse.from(order);
    }

    public OrderDto.OrderResponse createOrder(Long userId, OrderDto.OrderRequest request, List<MultipartFile> files) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "제조사를 찾을 수 없습니다."));

        String paymentId = UUID.randomUUID().toString();
        Order order = Order.builder()
                .user(user)
                .manufacturer(manufacturer)
                .deliveryAddress(request.getDeliveryAddress())
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .recipientEmail(request.getRecipientEmail())
                .specialRequest(request.getSpecialRequest())
                .purchasePrice(request.getPurchasePrice())
                .status(OrderStatus.ORDERED)
                .paymentId(paymentId)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < request.getOrderItems().size(); i++) {
            OrderDto.OrderItemDto itemDto = request.getOrderItems().get(i);
            MultipartFile file = files.get(i);
            File savedFile = fileService.saveModel(file);
            Technology technology = technologyRepository.findById(itemDto.getTechnologyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기술 정보를 찾을 수 없습니다."));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .file(savedFile)
                    .technology(technology)
                    .widthSize(itemDto.getWidthSize())
                    .lengthSize(itemDto.getLengthSize())
                    .heightSize(itemDto.getHeightSize())
                    .magnification(itemDto.getMagnification())
                    .quantity(itemDto.getQuantity())
                    .build();
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        // 구매자 유저 레벨 업데이트
        user.setTotalPurchaseCount(user.getTotalPurchaseCount() + 1);
        user.setTotalPurchaseAmount(user.getTotalPurchaseAmount() + order.getPurchasePrice());
        userService.updateUserLevel(user);
        userRepository.save(user);

        // 판매자 레벨 업데이트
        User seller = manufacturer.getDashboardUser();
        seller.setTotalPrintedCount(seller.getTotalPrintedCount() + 1);
        seller.setTotalPrintedAmount(seller.getTotalPrintedAmount() + order.getPurchasePrice());
        userService.updateUserLevel(seller);
        userRepository.save(seller);

        log.info("[주문 생성] 사용자: {}, 수령인: {}, 주문금액: {}, 주문항목수: {}, 제조사: {}, 결제ID: {}",
                user.getUsername(),
                order.getRecipientName(),
                order.getPurchasePrice(),
                orderItems.size(),
                manufacturer.getName(),
                paymentId);

        return OrderDto.OrderResponse.from(order);
    }


    public OrderDto.OrderResponse cancelOrderByUser(Long userId, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(userId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (!order.getStatus().equals(OrderStatus.ORDERED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "취소할 수 없는 상태입니다.");
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

        log.info("[주문 취소] 사용자 ID: {}, 주문ID: {}, 주문상태: CANCELED", userId, orderId);

        return OrderDto.OrderResponse.from(orderRepository.save(order));
    }

    public OrderDto.OrderResponse changeOrderStatusByManufacturer(Long manufacturerId, Long orderId, OrderDto.OrderStatusChangeRequest statusChangeRequest) {
        Order order = orderRepository.findByManufacturerIdAndId(manufacturerId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (order.getStatus().equals(OrderStatus.CANCELED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다.");
        }

        OrderStatus newStatus = getOrderStatusFromKey(statusChangeRequest.getNewStatus());
        OrderStatus prevStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("[주문 상태 변경] 제조사ID: {}, 주문ID: {}, 이전상태: {}, 변경상태: {}", manufacturerId, orderId, prevStatus, newStatus);

        return OrderDto.OrderResponse.from(order);
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
        List<Order> orders = orderRepository.findAllByManufacturerIdExcludingOrdered(manufacturerId, Pageable.unpaged());
        return orders.stream()
                .map(OrderDto.OrderResponse::from)
                .collect(Collectors.toList());
    }

    public OrderDto.OrderResponse getOrderDetailForDashboardUser(Long dashboardUserId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        Manufacturer manufacturer = manufacturerRepository.findByDashboardUserId(dashboardUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "대시보드 사용자에게 매칭되는 제조사가 없습니다."));

        if (!order.getManufacturer().getId().equals(manufacturer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 사용자에게는 접근 권한이 없습니다.");
        }

        return OrderDto.OrderResponse.from(order);
    }
}
