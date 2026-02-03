package com.bumil.audio_fall_care.domain.recorder.entity;

import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recorders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Recorder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecorderStatus status = RecorderStatus.CONNECTED;

    public void updateStatus(RecorderStatus status) {
        this.status = status;
    }

    public void updateDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
