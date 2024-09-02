package com.example.crafteria_server.domain.file.entity;

import com.example.crafteria_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "file")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class File extends BaseEntity {
    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String extension;

    @Column(nullable = false)
    private String uuid;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Type type;
}
