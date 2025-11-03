package com.nckh.yte.security;

import com.nckh.yte.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
public class UserDetailsImpl implements UserDetails {
    private final UUID id;
    private final String username;
    private final String password;
    private final String fullName;
    private final boolean enabled;
    private final String roleName;
    private final Set<String> roles;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User u) {
        this.id = u.getId();
        this.username = u.getUsername();
        this.password = u.getPassword();
        this.fullName = u.getFullName();
        this.enabled = u.isEnabled();

        // DB chỉ chứa "ADMIN", "DOCTOR", "NURSE", "PATIENT"
        String dbRole = Optional.ofNullable(u.getRole())
                .map(r -> r.getName())
                .orElse("PATIENT");

        // ⚡️ authorities cần có prefix "ROLE_" cho Spring Security
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + dbRole));

        // Gửi ra FE vai trò không prefix
        this.roles = Set.of(dbRole);
        this.roleName = dbRole;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

    public String getFullName() { return fullName; }
}
