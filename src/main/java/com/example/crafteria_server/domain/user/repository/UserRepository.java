package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByOauth2Id(String oauth2Id);
    Optional<User> findByUsername(String username);
}
