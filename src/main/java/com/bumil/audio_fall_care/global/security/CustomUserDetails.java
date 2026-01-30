package com.bumil.audio_fall_care.global.security;

import com.bumil.audio_fall_care.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer {
    private final Long userId;
    private final String username;
    private String password;
    private final String email;

    public CustomUserDetails(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = null;
    }

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
