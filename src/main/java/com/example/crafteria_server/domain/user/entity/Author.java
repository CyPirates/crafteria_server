package com.example.crafteria_server.domain.user.entity;

import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.model.entity.Model;
import jakarta.persistence.*;
import lombok.*;
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
    @Builder.Default
    private int rating = 5;

    // 추가된 realname 필드
    @Column(name = "realname")
    private String realname;

    @Column()
    private String introduction;

    @Column(nullable = false)
    @Builder.Default
    private long modelCount = 0;

    @Column(nullable = false)
    @Builder.Default
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

    // User의 realname을 Author의 realname으로 설정하는 메서드
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.realname = user.getRealname();
        }
    }
}
