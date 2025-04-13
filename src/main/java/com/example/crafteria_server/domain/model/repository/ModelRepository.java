package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.model.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    @Query("SELECT m FROM Model m ORDER BY m.viewCount DESC")
    Page<Model> findAllOrderByViewCountDesc(Pageable pageable);

    Page<Model> findAllByAuthorIdOrderByCreateDateDesc(Long userId, Pageable pageable);

    List<Model> findByNameContainingOrDescriptionContaining(String name, String description);

    List<Model> findByNameContaining(String name);

    List<Model> findByDescriptionContaining(String description);

    @Query("SELECT m FROM Model m WHERE m.isDeleted = false ORDER BY m.viewCount DESC")
    Page<Model> findAllByIsDeletedFalseOrderByViewCountDesc(Pageable pageable);

    Page<Model> findAllByAuthorIdAndIsDeletedFalseOrderByCreateDateDesc(Long authorId, Pageable pageable);

    Optional<Model> findByIdAndIsDeletedFalse(Long modelId);
}
