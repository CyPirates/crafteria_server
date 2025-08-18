package com.example.crafteria_server.domain.user.repository;

import com.example.crafteria_server.domain.user.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findByRealnameContainingOrIntroductionContaining(String realname, String introduction);

    List<Author> findByRealnameContaining(String realname);

    List<Author> findByIntroductionContaining(String introduction);

    @Query("""
           SELECT a
           FROM Author a
           JOIN a.user u
           ORDER BY u.totalSalesCount DESC, u.totalSalesAmount DESC, a.id DESC
           """)
    Page<Author> findPopularAuthorsBySales(Pageable pageable);
}
