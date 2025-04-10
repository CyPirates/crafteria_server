package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByRealnameContainingOrIntroductionContaining(String realname, String introduction);

    List<Author> findByRealnameContaining(String realname);

    List<Author> findByIntroductionContaining(String introduction);
}
