package com.bumil.audio_fall_care.domain.history.entity;

import com.bumil.audio_fall_care.domain.recorder.entity.Recorder;
import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fall_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FallHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorder_id", nullable = false)
    private Recorder recorder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private String soundType;

    @Column(nullable = false)
    private LocalDateTime detectedAt;
}
