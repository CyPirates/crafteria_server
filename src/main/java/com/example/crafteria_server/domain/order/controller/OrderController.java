package com.example.crafteria_server.domain.order.controller;


import com.example.crafteria_server.domain.order.dto.OrderDto;
import com.example.crafteria_server.domain.order.service.OrderService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j(topic = "OrderController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/my")
    public JsonBody<List<OrderDto.OrderResponse>> getMyOrderList(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                 @RequestParam(defaultValue = "0") int page) {
        return JsonBody.of(200, "성공", orderService.getMyOrderList(principalDetails.getUserId(), page));
    }

    @GetMapping("/my/{orderId}")
    public JsonBody<OrderDto.OrderResponse> getOrderDetail(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                           @PathVariable Long orderId) {
        return JsonBody.of(200, "성공", orderService.getOrderDetail(principalDetails.getUserId(), orderId));
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public JsonBody<OrderDto.OrderResponse> createOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute OrderDto.OrderRequest request) {
        return JsonBody.of(200, "성공", orderService.createOrder(principalDetails.getUserId(), request));
    }

    @PostMapping("/my/cancel/{orderId}")
    public JsonBody<OrderDto.OrderResponse> cancelOrder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                        @PathVariable Long orderId) {
        return JsonBody.of(200, "주문이 취소되었습니다.", orderService.cancelOrderByUser(principalDetails.getUserId(), orderId));
    }

    @PostMapping("/manufacturer/change-status/{orderId}")
    public JsonBody<OrderDto.OrderResponse> changeOrderStatus(@RequestParam Long manufacturerId,
                                              @RequestParam String newStatusKey,
                                              @PathVariable Long orderId) {

        return JsonBody.of(200, "주문 상태가 변경되었습니다.",orderService.changeOrderStatusByManufacturer(manufacturerId, orderId, newStatusKey));
    }
}
