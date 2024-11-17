package com.example.crafteria_server.global.security;

import com.example.crafteria_server.domain.user.entity.User;
import com.example.crafteria_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public PrincipalDetails loadUserByUsername(String username) {
        Optional<User> findUser = userRepository.findByRealname(username);
        return findUser.map(PrincipalDetails::new).orElse(null);
    }

    public PrincipalDetails loadUserById(Long id) {
        Optional<User> findUser = userRepository.findById(id);
        return findUser.map(PrincipalDetails::new).orElse(null);
    }

}
