package com.bumil.audio_fall_care.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(nullable = false)
    private String address;

    private String addressDetail;

    public  Address(String address, String addressDetail) {
        this.address = address;
        this.addressDetail = addressDetail;
    }
}
