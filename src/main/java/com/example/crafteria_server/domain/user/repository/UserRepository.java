package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.DashboardStatus;
import com.example.crafteria_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByOauth2Id(String oauth2Id);
    Optional<User> findByUsername(String username);

    Optional<User> findByRealname(String realname);
    boolean existsByUsername(String username);

    List<User> findByDashboardStatus(DashboardStatus status);

}
