package com.example.crafteria_server.domain.user.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.model.entity.Model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    @Column(name = "user_id")
    private Long id;

    // rating: 0 ~ 10
    @Column(nullable = false)
    private int rating = 5;

    @Column()
    private String introduction;

    @Column(nullable = false)
    private long modelCount = 0;

    @Column(nullable = false)
    private long viewCount = 0;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id")
    private File profileImage;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Model> models = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "author_id", referencedColumnName = "user_id")
    private User user;
}
