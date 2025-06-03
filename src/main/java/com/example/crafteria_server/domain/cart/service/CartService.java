package com.example.crafteria_server.domain.cart.service;

import com.example.crafteria_server.domain.cart.entity.Cart;
import com.example.crafteria_server.domain.cart.repository.CartRepository;
import com.example.crafteria_server.domain.manufacturer.entity.Manufacturer;
import com.example.crafteria_server.domain.manufacturer.repository.ManufacturerRepository;
import com.example.crafteria_server.domain.model.entity.Model;
import com.example.crafteria_server.domain.model.repository.ModelRepository;
import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "CartService")
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ModelRepository modelRepository;

    public Cart addToCart(Long userId, Long manufacturerId, Long modelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Manufacturer manufacturer = null;
        Model model = null;

        if (manufacturerId != null) {
            manufacturer = manufacturerRepository.findById(manufacturerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "제조사를 찾을 수 없습니다."));
        }

        if (modelId != null) {
            model = modelRepository.findById(modelId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "모델을 찾을 수 없습니다."));
        }

        Optional<Cart> existingCart = cartRepository.findByUserIdAndManufacturerIdAndModelId(userId, manufacturerId, modelId);
        if (existingCart.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "장바구니에 이미 해당 항목이 있습니다.");
        }

        Cart cart = Cart.builder()
                .user(user)
                .manufacturer(manufacturer)
                .model(model)
                .build();

        Cart savedCart = cartRepository.save(cart);

        if (model != null) {
            log.info("장바구니 추가 - 사용자 ID: {}, 모델 ID: {}, 모델 이름: {}", user.getId(), model.getId(), model.getName());
        }
        if (manufacturer != null) {
            log.info("장바구니 추가 - 사용자 ID: {}, 제조사 ID: {}, 제조사 이름: {}", user.getId(), manufacturer.getId(), manufacturer.getName());
        }

        return savedCart;
    }

    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        if (!cart.getUser().getId().equals(userId)) {
            throw new SecurityException("이 장바구니 항목에 대한 접근이 허용되지 않습니다.");
        }

        cartRepository.delete(cart);

        log.info("장바구니 삭제 - 사용자 ID: {}, 삭제된 장바구니 ID: {}", userId, cartId);
    }

    public List<Cart> listCarts(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}
