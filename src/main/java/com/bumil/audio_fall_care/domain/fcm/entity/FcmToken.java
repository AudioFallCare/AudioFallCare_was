package com.bumil.audio_fall_care.domain.fcm.entity;

import com.bumil.audio_fall_care.domain.user.entity.User;
import com.bumil.audio_fall_care.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "fcm_tokens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_device",
                        columnNames = {"user_id", "device_info"}
                )
        }
)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(length = 500, nullable = false)
    private String token;

    @Column(name = "device_info", length = 255, nullable = false)
    private String deviceInfo;

    @Builder
    public FcmToken(User user, String token, String deviceInfo) {
        this.user = user;
        this.token = token;
        this.deviceInfo = deviceInfo;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
