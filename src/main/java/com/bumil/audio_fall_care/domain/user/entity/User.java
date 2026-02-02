package com.bumil.audio_fall_care.domain.user.entity;

import com.bumil.audio_fall_care.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Embedded
    private Address address;

    @Column(length = 6, unique = true)
    private String code;

    @Builder
    public User(String username, String password, String email, Address address, String code) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.address = address;
        this.code = code;
    }
}
