package com.example.crafteria_server.domain.cart.repository;

import com.example.crafteria_server.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId);

    // 동일 사용자, 제조사, 모델로 이미 존재하는 장바구니 항목을 찾는 메소드
    Optional<Cart> findByUserIdAndManufacturerIdAndModelId(Long userId, Long manufacturerId, Long modelId);
}
