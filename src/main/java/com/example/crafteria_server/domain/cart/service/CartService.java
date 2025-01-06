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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ModelRepository modelRepository;

    public Cart addToCart(Long userId, Long manufacturerId, Long modelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Manufacturer manufacturer = null;
        Model model = null;

        if (manufacturerId != null) {
            manufacturer = manufacturerRepository.findById(manufacturerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Manufacturer not found"));
        }

        if (modelId != null) {
            model = modelRepository.findById(modelId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Model not found"));
        }

        Cart cart = Cart.builder()
                .user(user)
                .manufacturer(manufacturer)
                .model(model)
                .build();
        return cartRepository.save(cart);
    }

    public void removeFromCart(Long userId, Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        if (!cart.getUser().getId().equals(userId)) {
            throw new SecurityException("Not allowed to access this cart");
        }
        cartRepository.delete(cart);
    }

    public List<Cart> listCarts(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}
