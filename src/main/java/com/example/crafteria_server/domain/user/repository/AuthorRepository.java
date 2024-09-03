package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
