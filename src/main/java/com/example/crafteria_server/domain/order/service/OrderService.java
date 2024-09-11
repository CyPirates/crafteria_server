package com.example.crafteria_server.domain.order.service;


import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.service.FileService;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.repository.ModelPurchaseRepository;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.order.dto.OrderDto;
import com.example.crafteria_server.domain.order.entity.Order;
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
import org.springframework.web.server.ResponseStatusException;


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

        /* ModelPurchase modelPurchase = modelPurchaseRepository.findByUserIdAndModelId(userId, request.getModelId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "구매한 도면을 찾을 수 없습니다."));*/

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // STL 파일 저장
        File savedModelFile = fileService.saveModel(request.getModelFile());

        // 제조사 조회
        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조사를 찾을 수 없습니다."));

        // 주문 생성
        Order order = Order.builder()
                .modelFile(savedModelFile)  // 모델 파일 설정
                .user(user)
                .manufacturer(manufacturer)
                .deliveryAddress(request.getDeliveryAddress())
                .status(OrderStatus.ORDERED)
                .widthSize(request.getWidthSize())
                .lengthSize(request.getLengthSize())
                .heightSize(request.getHeightSize())
                .quantity(request.getQuantity())
                .magnification(request.getMagnification())
                .purchasePrice(0)  // 구매 가격 설정 (필요시 수정)
                .build();

        // 주문 저장 후 응답
        return OrderDto.OrderResponse.from(orderRepository.save(order));
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
}
