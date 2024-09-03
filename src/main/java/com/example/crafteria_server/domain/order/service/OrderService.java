package com.example.crafteria_server.domain.order.service;


import com.example.crafteria_server.domain.model.entity.Model;
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


import java.util.List;

@Service
@Slf4j(topic = "OrderService")
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;


    public List<OrderDto.Response> getMyOrderList(Long userId, int page) {
        PageRequest pageable = PageRequest.of(page, 10);
        return orderRepository.findAllByUserId(userId, pageable).stream()
                .map(OrderDto.Response::from)
                .toList();
    }

    public OrderDto.Response getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByUserIdAndId(userId, orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        return OrderDto.Response.from(order);
    }

    public OrderDto.Response createOrder(Long userId, OrderDto.CreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        Model model = modelRepository.findById(request.getModelId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "도면을 찾을 수 없습니다."));

        if (model.getMinimumSize() > request.getModelSize() || model.getMaximumSize() < request.getModelSize()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈가 도면의 범위를 벗어납니다.");
        }

        Order order = Order.builder()
                .model(model)
                .user(user)
                .deliveryAddress(request.getDeliveryAddress())
                .status(OrderStatus.ORDERED)
                .modelSize(request.getModelSize())
                .purchasePrice(model.getPrice())
                .build();

        return OrderDto.Response.from(orderRepository.save(order));
    }
}
