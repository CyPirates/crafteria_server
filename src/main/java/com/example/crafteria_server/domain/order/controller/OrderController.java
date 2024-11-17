package com.example.crafteria_server.domain.order.controller;


import com.example.crafteria_server.domain.order.dto.OrderDto;
import com.example.crafteria_server.domain.order.service.OrderService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "내 주문 목록 조회", description = "내 주문 목록을 조회합니다.")
    @GetMapping("/my")
    public JsonBody<List<OrderDto.OrderResponse>> getMyOrderList(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                 @RequestParam(defaultValue = "0") int page) {
        return JsonBody.of(200, "성공", orderService.getMyOrderList(principalDetails.getUserId(), page));
    }

    @Operation(summary = "주문 상세 조회", description = "주문 상세를 조회합니다.")
    @GetMapping("/my/{orderId}")
    public JsonBody<OrderDto.OrderResponse> getOrderDetail(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                           @PathVariable Long orderId) {
        return JsonBody.of(200, "성공", orderService.getOrderDetail(principalDetails.getUserId(), orderId));
    }

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public JsonBody<OrderDto.OrderResponse> createOrder(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute OrderDto.OrderRequest request) {
        return JsonBody.of(200, "성공", orderService.createOrder(principalDetails.getUserId(), request));
    }

    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PostMapping("/my/cancel/{orderId}")
    public JsonBody<OrderDto.OrderResponse> cancelOrder(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                        @PathVariable Long orderId) {
        return JsonBody.of(200, "주문이 취소되었습니다.", orderService.cancelOrderByUser(principalDetails.getUserId(), orderId));
    }

    @Operation(summary = "제조사에서 주문 상태 변경", description = "주문 상태를 변경합니다.")
    @PostMapping("/manufacturer/change-status/{orderId}")
    public JsonBody<OrderDto.OrderResponse> changeOrderStatus(@RequestParam Long manufacturerId,
                                              @RequestParam String newStatusKey,
                                              @PathVariable Long orderId) {

        return JsonBody.of(200, "주문 상태가 변경되었습니다.",orderService.changeOrderStatusByManufacturer(manufacturerId, orderId, newStatusKey));
    }

    @Operation(summary = "특정 제조사로 주문된 상태가 ORDERED인 모든 주문 조회", description = "특정 제조사로 주문된 상태가 ORDERED인 모든 주문을 조회합니다.")
    @GetMapping("/manufacturer/{manufacturerId}/ordered")
    public JsonBody<List<OrderDto.OrderResponse>> getOrderedOrdersByManufacturer(@PathVariable Long manufacturerId) {
        return JsonBody.of(200, "성공", orderService.getOrderedOrdersByManufacturer(manufacturerId));
    }

    @Operation(summary = "특정 제조사로 주문된 모든 주문 조회", description = "특정 제조사로 주문된 모든 주문을 조회합니다.")
    @GetMapping("/manufacturer/{manufacturerId}/orders")
    public JsonBody<List<OrderDto.OrderResponse>> getAllOrdersByManufacturer(@PathVariable Long manufacturerId) {
        return JsonBody.of(200, "성공", orderService.getAllOrdersByManufacturer(manufacturerId));
    }
}
