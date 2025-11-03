package com.example.crafteria_server.domain.model.repository;

import com.example.crafteria_server.domain.chart.dto.UserMonthlySalesStatisticsProjection;
import com.example.crafteria_server.domain.chart.dto.UserSalesStatisticsProjection;
import com.example.crafteria_server.domain.model.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    Page<Model> findAllByIsDeletedFalseOrderByDownloadCountDesc(Pageable pageable);

    // ✅ 무료 도면 (price == 0)
    Page<Model> findAllByIsDeletedFalseAndPriceEqualsOrderByCreateDateDesc(long price, Pageable pageable);

    // ✅ 유료 도면 (price > 0)
    Page<Model> findAllByIsDeletedFalseAndPriceGreaterThanOrderByCreateDateDesc(long price, Pageable pageable);

    @Query("""
        select m
        from Model m
        left join fetch m.assets a
        left join fetch a.file f
        where m.id = :id
    """)
    Optional<Model> findWithAssetsById(@Param("id") Long id);

}
