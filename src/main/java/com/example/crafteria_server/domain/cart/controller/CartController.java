package com.example.crafteria_server.domain.cart.controller;

import com.example.crafteria_server.domain.cart.dto.CartDto;
import com.example.crafteria_server.domain.cart.entity.Cart;
import com.example.crafteria_server.domain.cart.service.CartService;
import com.example.crafteria_server.global.response.JsonBody;
import com.example.crafteria_server.global.security.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/addManufacturer")
    public ResponseEntity<JsonBody<CartDto.CartResponse>> addManufacturerToCart(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                                @RequestBody CartDto.CartRequest cartRequest) {
        Cart cart = cartService.addToCart(principalDetails.getUser().getId(),
                cartRequest.getManufacturerId(), null);
        return ResponseEntity.ok(JsonBody.of(200, "Manufacturer added successfully", CartDto.CartResponse.from(cart)));
    }

    @PostMapping("/addModel")
    public ResponseEntity<JsonBody<CartDto.CartResponse>> addModelToCart(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                                         @RequestBody CartDto.CartRequest cartRequest) {
        Cart cart = cartService.addToCart(principalDetails.getUser().getId(),
                null, cartRequest.getModelId());
        return ResponseEntity.ok(JsonBody.of(200, "Model added successfully", CartDto.CartResponse.from(cart)));
    }

    @GetMapping
    public ResponseEntity<JsonBody<List<CartDto.CartResponse>>> listCarts(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        List<Cart> carts = cartService.listCarts(principalDetails.getUser().getId());
        List<CartDto.CartResponse> responses = carts.stream().map(CartDto.CartResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(JsonBody.of(200, "Cart list retrieved successfully", responses));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<JsonBody<Void>> removeFromCart(@AuthenticationPrincipal PrincipalDetails principalDetails,
                                                         @PathVariable Long cartId) {
        cartService.removeFromCart(principalDetails.getUser().getId(), cartId);
        return ResponseEntity.ok(JsonBody.of(200, "Cart item removed successfully", null));
    }
}
