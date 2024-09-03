package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.model.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    @Query("SELECT m FROM Model m ORDER BY m.viewCount DESC")
    Page<Model> findAllOrderByViewCountDesc(Pageable pageable);
}
