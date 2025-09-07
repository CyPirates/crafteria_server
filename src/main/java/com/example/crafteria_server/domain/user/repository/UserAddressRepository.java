package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    List<UserAddress> findAllByUserId(Long userId);  // ← 이 메서드를 추가
}
