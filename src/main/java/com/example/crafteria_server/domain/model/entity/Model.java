package com.example.crafteria_server.domain.model.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.user.entity.Author;
import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "model")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Model extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    // rating: 0 ~ 10
    @Column(nullable = false)
    private int rating = 5;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private long viewCount = 0;

    @Column(nullable = false)
    private long downloadCount = 0;

    @Column(nullable = false)
    private int minimumSize;

    @Column(nullable = false)
    private int maximumSize;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "model_file_id")
    private File modelFile;
}
