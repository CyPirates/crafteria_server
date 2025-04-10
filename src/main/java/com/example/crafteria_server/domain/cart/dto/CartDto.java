package com.example.crafteria_server.domain.cart.dto;

import com.example.crafteria_server.domain.cart.entity.Cart;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.model.entity.Model;
import lombok.*;


public class CartDto {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartRequest {
        private Long manufacturerId;
        private Long modelId;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartResponse {
        private Long cartId;
        private Long userId;
        private Long manufacturerId;
        private Long modelId;

        public static CartResponse from(Cart cart) {
            CartResponse response = new CartResponse();
            response.setCartId(cart.getId());
            response.setUserId(cart.getUser().getId());

            Manufacturer manufacturer = cart.getManufacturer();
            if (manufacturer != null) {
                response.setManufacturerId(manufacturer.getId());
            } else {
                response.setManufacturerId(null);  // 또는 적절한 기본값 설정
            }

            Model model = cart.getModel();
            if (model != null) {
                response.setModelId(model.getId());
            } else {
                response.setModelId(null);  // 또는 적절한 기본값 설정
            }

            return response;
        }
    }
}
